package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.model.*
import java.util.concurrent.*

object ParadoxMemberConfigResolver {
    fun getConfigs(definitionMemberInfo: ParadoxDefinitionMemberInfo, matchOptions: Int = Options.Default): List<CwtMemberConfig<*>> {
        val configsMap = doGetConfigsCacheFromCache(definitionMemberInfo.element) ?: return emptyList()
        val definitionInfo = definitionMemberInfo.definitionInfo
        //这里需要特别处理缓存的键
        val cacheKey = buildString {
            append("dm#")
            append(matchOptions)
            val configContext = definitionInfo.getDeclaration(matchOptions)?.getUserData(CwtMemberConfig.Keys.configContextKey)
            if(configContext != null) {
                val cacheKeyFromInjectors = CwtDeclarationConfigInjector.getCacheKey(configContext, configContext.injectors)
                if(cacheKeyFromInjectors != null) {
                    append('#').append(cacheKeyFromInjectors)
                }
            }
        }
        return configsMap.computeIfAbsent(cacheKey) { doGetConfigs(definitionMemberInfo, matchOptions) }
    }
    
    fun getChildConfigs(definitionMemberInfo: ParadoxDefinitionMemberInfo, matchOptions: Int = Options.Default): List<CwtMemberConfig<*>> {
        val configsMap = doGetConfigsCacheFromCache(definitionMemberInfo.element) ?: return emptyList()
        val definitionInfo = definitionMemberInfo.definitionInfo
        //这里需要特别处理缓存的键
        val cacheKey = buildString {
            append("dmc#")
            append(matchOptions)
            val configContext = definitionInfo.getDeclaration(matchOptions)?.getUserData(CwtMemberConfig.Keys.configContextKey)
            if(configContext != null) {
                val cacheKeyFromInjectors = CwtDeclarationConfigInjector.getCacheKey(configContext, configContext.injectors)
                if(cacheKeyFromInjectors != null) {
                    append('#').append(cacheKeyFromInjectors)
                }
            }
        }
        return configsMap.computeIfAbsent(cacheKey) { doGetChildConfigs(definitionMemberInfo, matchOptions) }
    }
    
    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtMemberConfig<*>>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedMemberConfigsCacheKey) {
            val value = ConcurrentHashMap<String, List<CwtMemberConfig<*>>>()
            //invalidated on ScriptFileTracker
            //to optimize performance, do not invoke file.containingFile here
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetConfigs(definitionMemberInfo: ParadoxDefinitionMemberInfo, matchOptions: Int): List<CwtMemberConfig<*>> {
        val definitionInfo = definitionMemberInfo.definitionInfo
        val element = definitionMemberInfo.element
        //基于keyExpression，valueExpression可能不同
        val declaration = definitionInfo.getDeclaration(matchOptions) ?: return emptyList()
        //如果路径中可能待遇参数，则不进行解析
        val elementPath = definitionMemberInfo.elementPath
        if(elementPath.isParameterized) return emptyList()
        if(elementPath.isEmpty()) return declaration.toSingletonList()
        
        var result: List<CwtMemberConfig<*>> = declaration.toSingletonList()
        
        val configGroup = definitionMemberInfo.configGroup
        elementPath.subPaths.forEachFast f1@{ (_, subPath, isQuoted, isKey) ->
            ProgressManager.checkCanceled()
            
            //如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
            //如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则内联此内联规则
            
            val expression = ParadoxDataExpression.resolve(subPath, isQuoted, true)
            val nextResult = mutableListOf<CwtMemberConfig<*>>()
            
            run r1@{
                result.forEachFast f2@{ parentConfig ->
                    //处理内联规则
                    if(isKey && parentConfig is CwtPropertyConfig) {
                        val inlineStatus = ParadoxConfigInlineHandler.inlineByInlineConfig(element, subPath, isQuoted, parentConfig, nextResult)
                        if(inlineStatus) return@r1
                    }
                    
                    val configs = parentConfig.configs
                    if(configs.isNullOrEmpty()) return@f2
                    configs.forEachFast f3@{ config ->
                        if(isKey && config is CwtPropertyConfig) {
                            if(ParadoxConfigMatcher.matches(element, expression, config.keyExpression, config, configGroup, matchOptions).get(matchOptions)) {
                                ParadoxConfigInlineHandler.inlineByConfig(element, subPath, isQuoted, config, nextResult, matchOptions)
                            }
                        } else if(!isKey && config is CwtValueConfig) {
                            nextResult.add(config)
                        }
                    }
                }
            }
            
            result = nextResult
            
            //如过结果不为空且结果中存在需要重载的规则，则全部替换成重载后的规则
            run {
                if(result.isEmpty()) return@run
                val optimizedResult = mutableListOf<CwtMemberConfig<*>>()
                result.forEachFast { config ->
                    val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(element, config)
                    if(overriddenConfigs.isNotNullOrEmpty()) {
                        //这里需要再次进行匹配
                        overriddenConfigs.forEachFast { overriddenConfig ->
                            if(ParadoxConfigMatcher.matches(element, expression, overriddenConfig.expression, overriddenConfig, configGroup, matchOptions).get(matchOptions)) {
                                optimizedResult.add(overriddenConfig)
                            }
                        }
                    } else {
                        optimizedResult.add(config)
                    }
                    result = optimizedResult
                }
            }
            
            //如果结果不唯一且结果中存在按常量字符串匹配的规则，则仅选用那些规则
            run {
                if(result.size <= 1) return@run
                val optimizedResult = mutableListOf<CwtMemberConfig<*>>()
                result.forEachFast { config ->
                    if(config.expression.type == CwtDataType.Constant) optimizedResult.add(config)
                }
                if(optimizedResult.isNotEmpty()) {
                    result = optimizedResult
                    return@run
                }
            }
        }
        
        return result.sortedByPriority(configGroup) { it.expression }
    }
    
    private fun doGetChildConfigs(definitionMemberInfo: ParadoxDefinitionMemberInfo, matchOptions: Int): List<CwtMemberConfig<*>> {
        val definitionInfo = definitionMemberInfo.definitionInfo
        //得到的是合并后的规则列表，且过滤重复的
        //基于上一级keyExpression，keyExpression一定唯一
        val declaration = definitionInfo.getDeclaration(matchOptions) ?: return emptyList()
        if(declaration.configs.isNullOrEmpty()) return emptyList()
        //如果路径中可能待遇参数，则不进行解析
        val elementPath = definitionMemberInfo.elementPath
        if(elementPath.isParameterized) return emptyList()
        //parentPath可以对应property或者value
        return when {
            //这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
            elementPath.isEmpty() -> declaration.configs.orEmpty()
            else -> {
                //打平propertyConfigs中的每一个properties
                val configs = doGetConfigs(definitionMemberInfo, matchOptions)
                val result = mutableListOf<CwtMemberConfig<*>>()
                configs.forEachFast { config ->
                    val childConfigs = config.configs
                    if(childConfigs.isNotNullOrEmpty()) result.addAll(childConfigs)
                }
                result
            }
        }
    }
}
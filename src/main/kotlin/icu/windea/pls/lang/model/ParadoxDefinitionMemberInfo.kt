package icu.windea.pls.lang.model

import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

/**
 * @property elementPath 相对于所属定义的定义成员路径。
 */
class ParadoxDefinitionMemberInfo(
    val elementPath: ParadoxElementPath,
    val gameType: ParadoxGameType,
    val definitionInfo: ParadoxDefinitionInfo,
    val configGroup: CwtConfigGroup,
    val element: ParadoxScriptMemberElement
    //element直接作为属性的话可能会有些问题，不过这个缓存会在所在脚本文件变更时被清除，应当问题不大
    //element不能转为SmartPsiElementPointer然后作为属性，这会导致与ParadoxDefinitionInfo.element引发递归异常
) {
    val isDefinition = element is ParadoxScriptDefinitionElement && elementPath.isEmpty()
    val isParameterized = elementPath.isParameterized
    
    private val cache: MutableMap<String, List<CwtDataConfig<*>>> = ConcurrentHashMap()
    
    /**
     * 对应的配置列表。
     */
    fun getConfigs(matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtDataConfig<*>> {
        var cacheKey = "$matchOptions"
        //这里需要特别处理缓存的键
        val configContext = definitionInfo.getDeclaration(matchOptions)?.getUserData(CwtDataConfig.Keys.configContextKey)
        if(configContext != null) {
            cacheKey = CwtDeclarationConfigInjector.getCacheKey(cacheKey, configContext, configContext.injectors) ?: cacheKey
        }
        return cache.getOrPut(cacheKey) { doGetConfigs(definitionInfo, this, matchOptions) }
    }
    
    /**
     * 对应的子配置列表。（得到的是合并后的规则列表，且过滤重复的）
     */
    fun getChildConfigs(matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtDataConfig<*>> {
        var cacheKey = "child#$matchOptions"
        //这里需要特别处理缓存的键
        val configContext = definitionInfo.getDeclaration(matchOptions)?.getUserData(CwtDataConfig.Keys.configContextKey)
        if(configContext != null) {
            cacheKey = CwtDeclarationConfigInjector.getCacheKey(cacheKey, configContext, configContext.injectors) ?: cacheKey
        }
        return cache.getOrPut(cacheKey) { doGetChildConfigs(definitionInfo, this, matchOptions) }
    }
}

/**
 * 根据路径解析对应的属性/值配置列表。
 */
private fun doGetConfigs(definitionInfo: ParadoxDefinitionInfo, definitionMemberInfo: ParadoxDefinitionMemberInfo, matchOptions: Int): List<CwtDataConfig<*>> {
    val element = definitionMemberInfo.element
    //基于keyExpression，valueExpression可能不同
    val declaration = definitionInfo.getDeclaration(matchOptions) ?: return emptyList()
    //如果路径中可能待遇参数，则不进行解析
    val elementPath = definitionMemberInfo.elementPath
    if(elementPath.isParameterized) return emptyList()
    if(elementPath.isEmpty()) return declaration.toSingletonList()
    
    var result: List<CwtDataConfig<*>> = declaration.toSingletonList()
    
    var inlinedByInlineConfig = false
    
    val configGroup = definitionMemberInfo.configGroup
    elementPath.subPathInfos.forEachFast f1@{ info ->
        val (key, isQuoted, isKey) = info
        
        //如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
        //如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则内联此内联规则
        
        val expression = ParadoxDataExpression.resolve(key, isQuoted, true)
        val nextResult = SmartList<CwtDataConfig<*>>()
        result.forEachFast f2@{ parentConfig ->
            ProgressManager.checkCanceled()
            
            //处理内联规则
            if(!inlinedByInlineConfig && isKey && parentConfig is CwtPropertyConfig) {
                inlinedByInlineConfig = ParadoxConfigInlineHandler.inlineFromInlineConfig(element, key, isQuoted, parentConfig, nextResult)
                if(inlinedByInlineConfig) return@f2
            }
            
            val configs = parentConfig.configs
            if(configs.isNullOrEmpty()) return@f2
            configs.forEachFast f3@{ config ->
                if(isKey && config is CwtPropertyConfig) {
                    if(ParadoxConfigMatcher.matches(element, expression, config.keyExpression, config, configGroup, matchOptions).get()) {
                        ParadoxConfigInlineHandler.inlineFromAliasConfig(element, key, isQuoted, config, nextResult, matchOptions)
                    }
                } else if(!isKey && config is CwtValueConfig) {
                    nextResult.add(config)
                }
            }
        }
        
        result = nextResult
        
        //如过结果不为空且结果中存在需要重载的规则，则全部替换成重载后的规则
        run {
            if(result.isEmpty()) return@run
            val optimizedResult = SmartList<CwtDataConfig<*>>()
            result.forEachFast { config ->
                val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(element, config)
                if(overriddenConfigs.isNotNullOrEmpty()) {
                    //这里需要再次进行匹配
                    overriddenConfigs.forEachFast { overriddenConfig ->
                        if(ParadoxConfigMatcher.matches(element, expression, overriddenConfig.expression, overriddenConfig, configGroup, matchOptions).get()) {
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
            val optimizedResult = SmartList<CwtDataConfig<*>>()
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

/**
 * 根据路径解析对应的子属性规则列表。（得到的是合并后的规则列表，且过滤重复的）
 */
private fun doGetChildConfigs(definitionInfo: ParadoxDefinitionInfo, definitionMemberInfo: ParadoxDefinitionMemberInfo, matchOptions: Int): List<CwtDataConfig<*>> {
    //基于上一级keyExpression，keyExpression一定唯一
    val declaration = definitionInfo.getDeclaration(matchOptions) ?: return emptyList()
    if(declaration.configs.isNullOrEmpty()) return emptyList()
    //如果路径中可能待遇参数，则不进行解析
    val elementPath = definitionMemberInfo.elementPath
    if(elementPath.isParameterized) return emptyList()
    //parentPath可以对应property或者value
    return when {
        //这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
        elementPath.isEmpty() -> declaration.configs
        else -> {
            //打平propertyConfigs中的每一个properties
            val configs = doGetConfigs(definitionInfo, definitionMemberInfo, matchOptions)
            val result = SmartList<CwtDataConfig<*>>()
            configs.forEachFast { config ->
                val childConfigs = config.configs
                if(childConfigs.isNotNullOrEmpty()) result.addAll(childConfigs)
            }
            result
        }
    }
}

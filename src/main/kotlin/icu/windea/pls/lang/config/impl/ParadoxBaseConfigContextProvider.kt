package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取直接的CWT规则上下文。
 */
class ParadoxBaseConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(contextElement: PsiElement, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val fileInfo = vFile.fileInfo ?: return null
        val elementPath = ParadoxElementPathHandler.getFromFile(contextElement) ?: return null
        val definition = contextElement.findParentDefinition()
        val type = ParadoxConfigHandler.getContextType(contextElement)
        if(definition == null) {
            return ParadoxConfigContext(type, fileInfo, elementPath)
        } else {
            val definitionInfo = definition.definitionInfo ?: return null
            val definitionElementPath = definitionInfo.elementPath
            val elementPathFromDefinition = definitionElementPath.relativeTo(elementPath) ?: return null
            return ParadoxConfigContext(type, fileInfo, elementPath, definitionInfo, elementPathFromDefinition)
        }
    }
    
    override fun getConfigs(contextElement: PsiElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        TODO("Not yet implemented")
    }
    
    private fun doGetConfigs(contextElement: PsiElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val definitionInfo = configContext.definitionInfo ?: return null
        val elementPath = configContext.elementPathFromDefinition ?: return null
        if(elementPath.isParameterized) return null //skip if element path is parameterized
        
        //基于keyExpression，valueExpression可能不同
        val declaration = definitionInfo.getDeclaration(matchOptions) ?: return null
        if(elementPath.isEmpty()) return declaration.toSingletonList()
        
        var result: List<CwtMemberConfig<*>> = declaration.toSingletonList()
        
        val configGroup = definitionInfo.configGroup
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
                        val inlineStatus = ParadoxConfigInlineHandler.inlineByInlineConfig(contextElement, subPath, isQuoted, parentConfig, nextResult)
                        if(inlineStatus) return@r1
                    }
                    
                    val configs = parentConfig.configs
                    if(configs.isNullOrEmpty()) return@f2
                    configs.forEachFast f3@{ config ->
                        if(isKey && config is CwtPropertyConfig) {
                            if(ParadoxConfigMatcher.matches(contextElement, expression, config.keyExpression, config, configGroup, matchOptions).get(matchOptions)) {
                                ParadoxConfigInlineHandler.inlineByConfig(contextElement, subPath, isQuoted, config, nextResult, matchOptions)
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
                    val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(contextElement, config)
                    if(overriddenConfigs.isNotNullOrEmpty()) {
                        //这里需要再次进行匹配
                        overriddenConfigs.forEachFast { overriddenConfig ->
                            if(ParadoxConfigMatcher.matches(contextElement, expression, overriddenConfig.expression, overriddenConfig, configGroup, matchOptions).get(matchOptions)) {
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
    
    private fun doGetChildConfigs(contextElement: PsiElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val definitionInfo = configContext.definitionInfo ?: return null
        val elementPath = configContext.elementPathFromDefinition ?: return null
        if(elementPath.isParameterized) return null //skip if element path is parameterized
        
        //得到的是合并后的规则列表，且过滤重复的
        //基于上一级keyExpression，keyExpression一定唯一
        val declaration = definitionInfo.getDeclaration(matchOptions) ?: return null
        if(declaration.configs.isNullOrEmpty()) return null
        //parentPath可以对应property或者value
        return when {
            //这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
            elementPath.isEmpty() -> declaration.configs.orEmpty()
            else -> {
                //打平propertyConfigs中的每一个properties
                val configs = doGetConfigs(contextElement, configContext, matchOptions) ?: return null
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

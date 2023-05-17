package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 基于CWT规则过滤代码检查。
 */
class ParadoxScriptConfigAwareInspectionSuppressor : InspectionSuppressor {
    //ParadoxScriptUnresolvedExpression - 从定义级别向下检查
    //其他 - 一般从成员级别或表达式级别直接检查，需要一定的兼容性处理
    
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if(element is ParadoxScriptDefinitionElement) {
            val definitionInfo = element.definitionInfo
            if(definitionInfo != null) {
                if(isSuppressed(definitionInfo.typeConfig.config, null, toolId)) return true
                if(definitionInfo.subtypeConfigs.any { isSuppressed(it.config, null, toolId) }) return true
            }
        }
        
        if(element is ParadoxScriptProperty || (element is ParadoxScriptExpressionElement && element.isExpression())) {
            val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
            if(configs.isNotEmpty()) {
                for(config in configs) {
                    //检查对应的规则
                    val configToUse = when {
                        config is CwtValueConfig && config.propertyConfig != null -> config.propertyConfig
                        else -> config
                    }
                    if(isSuppressed(configToUse, null, toolId)) return true
                    //向上检查对应的规则的所有父规则，直到不存在或者是内联的父规则为止
                    configToUse.processParent { c ->
                        if(c is CwtPropertyConfig && c.inlineableConfig != null) {
                            false
                        } else {
                            if(isSuppressed(c, config, toolId)) return true
                            true
                        }
                    }
                }
            }
        }
        
        return false
    }
    
    private fun isSuppressed(config: CwtDataConfig<*>, baseConfig: CwtDataConfig<*>?,  toolId: String): Boolean {
        //对于被重载的规则下面的某些规则，跳过ParadoxScriptMissingExpression
        if(toolId == "ParadoxScriptMissingExpression" && config.isOverridden == true) {
            if(baseConfig is CwtPropertyConfig && config == baseConfig.parent) {
                if(baseConfig.key.let { it == "value" || it == "count" }) {
                    return true
                }
            }
        }
        //基于"## suppress TOOL_ID"，跳过对应的代码检查
        if(config.options?.any { it.key == "suppress" && it.stringValue == toolId } == true) return true
        return false
    }
    
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY
    }
}
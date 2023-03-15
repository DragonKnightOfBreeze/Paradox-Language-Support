package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 基于CWT规则过滤代码检查。
 */
class CwtConfigAwareInspectionSuppressor : InspectionSuppressor {
    //UnresolvedExpressionInspection - 从定义级别向下检查
    //<others> - 一般从成员级别或表达式级别直接检查，需要一定的兼容性处理
    
    //TODO 验证是否可以生效
    
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            if(definitionInfo.typeConfig.config.isSuppressed(toolId)) return true
            if(definitionInfo.subtypeConfigs.any { it.config.isSuppressed(toolId) }) return true
        }
        
        val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
        if(configs.isNotEmpty()) {
            for(config in configs) {
                //检查对应的规则
                val configToUse = when {
                    config is CwtValueConfig && config.propertyConfig != null -> config.propertyConfig
                    else -> config
                }
                if(configToUse.isSuppressed(toolId)) return true
                //向上检查对应的规则的所有父规则，直到不存在或者是内联的父规则为止
                configToUse.processParent { c ->
                    if(c is CwtPropertyConfig && c.inlineableConfig != null) {
                        false
                    } else {
                        if(c.isSuppressed(toolId)) return true
                        true
                    }
                }
            }
        }
        
        return false
    }
    
    private fun CwtDataConfig<*>.isSuppressed(toolId: String): Boolean {
        return options?.any {
            it.key == "suppress" && it.stringValue == toolId
        } ?: false
    }
    
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY
    }
}
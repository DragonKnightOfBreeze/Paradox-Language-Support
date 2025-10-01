package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.processParent
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.isExpression

/**
 * 基于CWT规则，禁用适用于脚本文件的代码检查。
 *
 * TODO 暂未使用，需要验证
 */
class ParadoxScriptConfigAwareInspectionSuppressor : InspectionSuppressor {
    // ParadoxScriptUnresolvedExpression - 从定义级别向下检查
    // 其他 - 一般从成员级别或表达式级别直接检查，需要一定的兼容性处理

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (element is ParadoxScriptDefinitionElement) {
            val definitionInfo = element.definitionInfo
            if (definitionInfo != null) {
                if (isSuppressed(definitionInfo.typeConfig.config, toolId)) return true
                if (definitionInfo.subtypeConfigs.any { isSuppressed(it.config, toolId) }) return true
            }
        }

        if (element is ParadoxScriptProperty || (element is ParadoxScriptExpressionElement && element.isExpression())) {
            val configs = ParadoxExpressionManager.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
            if (configs.isNotEmpty()) {
                for (config in configs) {
                    // 检查对应的规则
                    val configToUse = when {
                        config is CwtValueConfig && config.propertyConfig != null -> config.propertyConfig!!
                        else -> config
                    }
                    if (isSuppressed(configToUse, toolId)) return true
                    // 向上检查对应的规则的所有父规则，直到不存在或者是内联的父规则为止
                    configToUse.processParent { c ->
                        if (c is CwtPropertyConfig && c.singleAliasConfig != null && c.aliasConfig != null && c.inlineConfig != null) {
                            false
                        } else {
                            if (isSuppressed(c, toolId)) return true
                            true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun isSuppressed(config: CwtMemberConfig<*>, toolId: String): Boolean {
        // 基于 "## suppress TOOL_ID"，跳过对应的代码检查
        if (config.optionData { suppressSet }.contains(toolId)) return true
        return false
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY
    }
}

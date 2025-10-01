package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.config.overriddenProvider
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.codeInsight.expression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.isExpression

/**
 * 对应的CWT规则有多个且存在冲突的表达式的检查。
 */
@Suppress("UNUSED_PARAMETER")
class ConflictingResolvedExpressionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptBlock) visitBlock(element)
            }

            private fun visitBlock(element: ParadoxScriptBlock) {
                if (!element.isExpression()) return // skip check if element is not an expression

                //skip checking property if its property key may contain parameters
                //position: (in property) property key / (standalone) left curly brace
                val property = element.parent
                    ?.castOrNull<ParadoxScriptProperty>()
                val position = property?.propertyKey
                    ?.also { if (it.text.isParameterized()) return }
                    ?: element.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE }
                    ?: return
                val expression = property?.expression ?: element.expression
                val configs = ParadoxExpressionManager.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
                doCheck(element, position, configs, expression)
            }

            private fun doCheck(element: ParadoxScriptMember, position: PsiElement, configs: List<CwtMemberConfig<*>>, expression: String) {
                if (skipCheck(element, configs)) return
                val isKey = position is ParadoxScriptPropertyKey
                val description = when {
                    isKey -> PlsBundle.message("inspection.script.conflictingResolvedExpression.desc.1", expression)
                    else -> PlsBundle.message("inspection.script.conflictingResolvedExpression.desc.2", expression)
                }
                holder.registerProblem(position, description)
            }

            private fun skipCheck(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Boolean {
                //子句可以精确匹配多个子句规则时，适用此检查
                if (configs.isEmpty()) return true
                //这里需要先按实际对应的规则位置去重
                if (configs.distinctBy { it.pointer }.size == 1) return true
                //如果是重载后提供的规则，跳过此检查
                if (isOverriddenConfigs(configs)) return true
                //如果存在规则，规则的子句中的所有key和value都可以分别被另一个规则的子句中的所有key和value包含，则仅使用这些规则
                val configsToCheck = filterConfigs(element, configs)
                if (configsToCheck.size == 1) return true
                return false
            }

            private fun isOverriddenConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
                return configs.any { it.memberConfig.castOrNull<CwtPropertyConfig>()?.overriddenProvider != null }
            }

            private fun filterConfigs(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
                val configsToCheck = configs.filter { config ->
                    val childConfigs = config.configs
                    childConfigs != null && configs.any { config0 ->
                        val childConfigs0 = config0.configs
                        config0 != config && childConfigs0 != null && childConfigs0.containsAll(childConfigs)
                    }
                }
                return configsToCheck.ifEmpty { configs }
            }
        }
    }
}

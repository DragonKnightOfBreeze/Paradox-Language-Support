package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.config.overriddenProvider
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.codeInsight.expression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.isExpression
import icu.windea.pls.script.psi.parentProperty
import javax.swing.JComponent

/**
 * 对应的规则有多个且存在冲突的表达式的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles 是否在内联脚本文件中忽略此代码检查。
 */
class ConflictingResolvedExpressionInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false
    @JvmField
    var ignoredInInlineScriptFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 判断是否需要忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        // 要求是符合条件的脚本文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptBlock) visitBlock(element)
            }

            private fun visitBlock(element: ParadoxScriptBlock) {
                ProgressManager.checkCanceled()
                if (!element.isExpression()) return // skip check if element is not an expression

                // skip checking property if its property key may contain parameters
                // position: (in property) property key / (standalone) left curly brace
                val property = element.parentProperty
                val position = property?.propertyKey
                    ?.also { if (it.text.isParameterized()) return }
                    ?: element.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE }
                    ?: return
                val expression = property?.expression ?: element.expression
                val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(acceptDefinition = true))
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
                // 子句可以精确匹配多个子句规则时，适用此检查
                if (configs.isEmpty()) return true
                // 这里需要先按实际对应的规则位置去重
                if (configs.distinctBy { it.pointer }.size == 1) return true
                // 如果是重载后提供的规则，跳过此检查
                if (isOverriddenConfigs(configs)) return true
                // 如果存在规则，规则的子句中的所有key和value都可以分别被另一个规则的子句中的所有key和value包含，则仅使用这些规则
                val configsToCheck = filterConfigs(element, configs)
                if (configsToCheck.size == 1) return true
                return false
            }

            private fun isOverriddenConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
                return configs.any { it.memberConfig.castOrNull<CwtPropertyConfig>()?.overriddenProvider != null }
            }

            @Suppress("UNUSED_PARAMETER")
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

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
            // ignoredInInlineScriptFiles
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles.toAtomicProperty())
            }
        }
    }
}

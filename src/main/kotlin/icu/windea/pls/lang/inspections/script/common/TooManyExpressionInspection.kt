package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 过多的表达式的检查。
 * @property firstOnly 是否仅标出第一个错误。
 * @property firstOnlyOnFile 在文件级别上，是否仅标出第一个错误。
 */
class TooManyExpressionInspection : LocalInspectionTool() {
    @JvmField
    var firstOnly = false
    @JvmField
    var firstOnlyOnFile = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxScriptBlock) visitBlock(element)
            }

            override fun visitFile(file: PsiFile) {
                if (file !is ParadoxScriptFile) return
                val configContext = ParadoxExpressionManager.getConfigContext(file) ?: return
                if (configContext.skipTooManyExpressionCheck()) return
                val configs = ParadoxExpressionManager.getConfigs(file, matchOptions = Options.Default or Options.AcceptDefinition)
                doCheck(file, file, configs)
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
                val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return
                if (configContext.skipTooManyExpressionCheck()) return
                val configs = ParadoxExpressionManager.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
                doCheck(element, position, configs)
            }

            private fun doCheck(element: ParadoxScriptMemberElement, position: PsiElement, configs: List<CwtMemberConfig<*>>) {
                if (skipCheck(element, configs)) return
                val occurrenceMap = ParadoxExpressionManager.getChildOccurrenceMap(element, configs)
                if (occurrenceMap.isEmpty()) return
                val overriddenProvider = getOverriddenProvider(configs)
                occurrenceMap.forEach { (configExpression, occurrence) ->
                    if (overriddenProvider != null && overriddenProvider.skipTooManyExpressionCheck(configs, configExpression)) return@forEach
                    val r = doCheckOccurrence(element, position, occurrence, configExpression)
                    if (!r) return
                }
            }

            private fun skipCheck(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Boolean {
                //子句不为空且可以精确匹配多个子句规则时，不适用此检查
                if (configs.isEmpty()) return true
                if (configs.size == 1) return false
                if (element is ParadoxScriptFile && element.block?.isEmpty == true) return false
                if (element is ParadoxScriptBlock && element.isEmpty) return false
                return true
            }

            private fun getOverriddenProvider(configs: List<CwtMemberConfig<*>>): CwtOverriddenConfigProvider? {
                configs.forEach { c1 ->
                    c1.overriddenProvider?.let { return it }
                    val pc1 = c1.castOrNull<CwtValueConfig>()?.propertyConfig
                    pc1?.overriddenProvider?.let { return it }
                    (pc1 ?: c1).processParent { c2 ->
                        c2.overriddenProvider?.let { return it }
                        true
                    }
                }
                return null
            }

            private fun doCheckOccurrence(element: ParadoxScriptMemberElement, position: PsiElement, occurrence: Occurrence, configExpression: CwtDataExpression): Boolean {
                val (actual, _, max, _, relaxMax) = occurrence
                if (max != null && actual > max) {
                    val isKey = configExpression.isKey
                    val isConst = configExpression.type == CwtDataTypes.Constant
                    val description = if (isKey) {
                        when {
                            isConst -> PlsBundle.message("inspection.script.tooManyExpression.desc.1.1", configExpression)
                            else -> PlsBundle.message("inspection.script.tooManyExpression.desc.1.2", configExpression)
                        }
                    } else {
                        when {
                            isConst -> PlsBundle.message("inspection.script.tooManyExpression.desc.2.1", configExpression)
                            else -> PlsBundle.message("inspection.script.tooManyExpression.desc.2.2", configExpression)
                        }
                    }
                    val maxDefine = occurrence.maxDefine
                    val detail = when {
                        maxDefine == null -> PlsBundle.message("inspection.script.tooManyExpression.desc.detail.1", max, actual)
                        else -> PlsBundle.message("inspection.script.tooManyExpression.desc.detail.2", max, actual, maxDefine)
                    }
                    val highlightType = when {
                        relaxMax -> ProblemHighlightType.WEAK_WARNING //weak warning (wave lines), not warning
                        else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    }
                    val fileLevel = element is PsiFile
                    if (!fileLevel && firstOnly && holder.hasResults()) return false
                    if (fileLevel && firstOnlyOnFile && holder.hasResults()) return false
                    holder.registerProblem(position, "$description $detail", highlightType)
                }
                return true
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            //firstOnly
            row {
                checkBox(PlsBundle.message("inspection.script.tooManyExpression.option.firstOnly"))
                    .bindSelected(::firstOnly)
                    .actionListener { _, component -> firstOnly = component.isSelected }
            }
            //firstOnlyOnFile
            row {
                checkBox(PlsBundle.message("inspection.script.tooManyExpression.option.firstOnlyOnFile"))
                    .bindSelected(::firstOnlyOnFile)
                    .actionListener { _, component -> firstOnlyOnFile = component.isSelected }
            }
        }
    }
}

package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.inline.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义声明中缺失的表达式的检查。
 * @property firstOnly 是否仅标出第一个错误。
 * @property firstOnlyOnFile 在文件级别上，是否仅标出第一个错误。（默认为true，否则文件顶部的错误列可能会过多）
 */
class MissingExpressionInspection : LocalInspectionTool() {
    @JvmField var firstOnly = false
    @JvmField var firstOnlyOnFile = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptBlock) visitBlock(element)
            }
            
            override fun visitFile(file: PsiFile) {
                if(file !is ParadoxScriptFile) return
                //忽略可能的脚本片段入口
                if(ParadoxScriptMemberElementInlineSupport.canLink(file)) return super.visitFile(file)
                val configs = ParadoxConfigHandler.getConfigs(file, allowDefinition = true)
                doCheck(file, file, configs)
            }
            
            private fun visitBlock(element: ParadoxScriptBlock) {
                //skip checking property if its property key may contain parameters
                //position: (in property) property key / (standalone) left curly brace
                val property = element.parent
                    ?.castOrNull<ParadoxScriptProperty>()
                //忽略可能的脚本片段入口
                if(property != null && ParadoxScriptMemberElementInlineSupport.canLink(property)) return
                val position = property?.propertyKey
                    ?.also { if(it.isParameterizedExpression()) return }
                    ?: element.findChild(ParadoxScriptElementTypes.LEFT_BRACE)
                    ?: return
                val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
                doCheck(element, position, configs)
            }
            
            private fun doCheck(element: ParadoxScriptMemberElement, position: PsiElement, configs: List<CwtDataConfig<*>>) {
                if(skipCheck(element, configs)) return
                val occurrenceMap = ParadoxConfigHandler.getChildOccurrenceMap(element, configs)
                if(occurrenceMap.isEmpty()) return
                occurrenceMap.forEach { (configExpression, occurrence) ->
                    val r = doCheckOccurrence(element, position, occurrence, configExpression)
                    if(!r) return
                }
            }
            
            private fun skipCheck(element: ParadoxScriptMemberElement, configs: List<CwtDataConfig<*>>): Boolean {
                //子句不为空且可以精确匹配多个子句规则时，不适用此检查
                if(configs.isEmpty()) return true
                if(configs.size == 1) return false
                if(element is ParadoxScriptFile && element.block?.isEmpty == true) return false
                if(element is ParadoxScriptBlock && element.isEmpty) return false
                return true
            }
            
            private fun doCheckOccurrence(element: ParadoxScriptMemberElement, position: PsiElement, occurrence: Occurrence, configExpression: CwtDataExpression): Boolean {
                val (actual, min, _, relaxMin) = occurrence
                if(min != null && actual < min) {
                    val isKey = configExpression is CwtKeyExpression
                    val isConst = configExpression.type == CwtDataType.Constant
                    val description = if(isKey) {
                        when {
                            isConst -> PlsBundle.message("inspection.script.general.missingExpression.description.1.1", configExpression)
                            else -> PlsBundle.message("inspection.script.general.missingExpression.description.1.2", configExpression)
                        }
                    } else {
                        when {
                            isConst -> PlsBundle.message("inspection.script.general.missingExpression.description.2.1", configExpression)
                            else -> PlsBundle.message("inspection.script.general.missingExpression.description.2.2", configExpression)
                        }
                    }
                    val minDefine = occurrence.minDefine
                    val detail = when {
                        minDefine == null -> PlsBundle.message("inspection.script.general.missingExpression.description.detail.1", min, actual)
                        else -> PlsBundle.message("inspection.script.general.missingExpression.description.detail.2", min, actual, minDefine)
                    }
                    val highlightType = when {
                        relaxMin -> ProblemHighlightType.WEAK_WARNING //weak warning (wave lines), not warning
                        else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    }
                    val fileLevel = element is PsiFile
                    if(!fileLevel && firstOnly && holder.hasResults()) return false
                    if(fileLevel && firstOnlyOnFile && holder.hasResults()) return false
                    holder.registerProblem(position, "$description $detail", highlightType)
                }
                return true
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            //firstOnly
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingExpression.option.firstOnly"))
                    .bindSelected(::firstOnly)
                    .actionListener { _, component -> firstOnly = component.isSelected }
            }
            //firstOnlyOnFile
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingExpression.option.firstOnlyOnFile"))
                    .bindSelected(::firstOnlyOnFile)
                    .actionListener { _, component -> firstOnlyOnFile = component.isSelected }
            }
        }
    }
}
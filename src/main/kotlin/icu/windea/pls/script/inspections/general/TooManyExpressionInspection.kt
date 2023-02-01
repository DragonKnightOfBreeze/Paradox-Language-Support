package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.cwt.expression.CwtDataType.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义声明中过多的表达式的检查。
 * @property firstOnly 是否仅标出第一个错误。
 * @property firstOnlyOnFile 在文件级别上，是否仅标出第一个错误。（默认为true，否则文件顶部的错误列可能会过多）
 */
class TooManyExpressionInspection: LocalInspectionTool() {
	@JvmField var firstOnly = false
	@JvmField var firstOnlyOnFile = true
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptBlock) visitBlock(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			override fun visitFile(file: PsiFile) {
				if(file !is ParadoxScriptFile) return
				val position = file //TODO not very suitable
				val definitionMemberInfo = file.definitionMemberInfo
				doCheck(position, definitionMemberInfo, true)
				super.visitFile(file)
			}
			
			private fun visitBlock(element: ParadoxScriptBlock) {
				ProgressManager.checkCanceled()
				//skip checking property if its property key may contain parameters
				//position: (in property) property key / (standalone) left curly brace
				val position = element.parent?.castOrNull<ParadoxScriptProperty>()?.propertyKey
					?.also { if(it.isParameterAwareExpression()) return }
					?: element.findChild(ParadoxScriptElementTypes.LEFT_BRACE)
					?: return
				val definitionMemberInfo = element.definitionMemberInfo
				doCheck(position, definitionMemberInfo, false)
			}
			
			private fun doCheck(position: PsiElement, definitionMemberInfo: ParadoxDefinitionMemberInfo?, fileLevel: Boolean) {
				if(definitionMemberInfo == null) return
				definitionMemberInfo.childPropertyOccurrenceMap.takeIf { it.isNotEmpty() }
					?.forEach { (configExpression, occurrence) ->
						val r = doCheckOccurrence(occurrence, configExpression, position, fileLevel)
						if(!r) return
					}
				definitionMemberInfo.childValueOccurrenceMap.takeIf { it.isNotEmpty() }
					?.forEach { (configExpression, occurrence) ->
						val r = doCheckOccurrence(occurrence, configExpression, position, fileLevel)
						if(!r) return
					}
			}
			
			private fun doCheckOccurrence(occurrence: Occurrence, configExpression: CwtDataExpression, position: PsiElement, fileLevel: Boolean): Boolean {
				val (actual, _, max) = occurrence
				if(max != null && actual > max) {
                    val isKey = configExpression is CwtKeyExpression
                    val isConst = configExpression.type == Constant
                    val description = if(isKey) {
                        when {
                            isConst -> PlsBundle.message("inspection.script.general.tooManyExpression.description.1.1", configExpression)
                            else -> PlsBundle.message("inspection.script.general.tooManyExpression.description.1.2", configExpression)
                        }
                    } else {
                        when {
                            isConst -> PlsBundle.message("inspection.script.general.tooManyExpression.description.2.1", configExpression)
                            else -> PlsBundle.message("inspection.script.general.tooManyExpression.description.2.2", configExpression)
                        }
                    }
					val maxDefine = occurrence.maxDefine
					val detail = when {
						maxDefine == null -> PlsBundle.message("inspection.script.general.tooManyExpression.description.detail.1", max, actual)
						else -> PlsBundle.message("inspection.script.general.tooManyExpression.description.detail.2", max, actual, maxDefine)
					}
                    val highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
					if(!fileLevel && firstOnly && holder.hasResults()) return false
					if(fileLevel && firstOnlyOnFile && holder.hasResults()) return false
                    holder.registerProblem(position, "$description $detail", highlightType)
                }
				return true
			}
		})
		return holder.resultsArray
	}
	
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			//firstOnly
			row {
				checkBox(PlsBundle.message("inspection.script.general.tooManyExpression.option.firstOnly"))
					.bindSelected(::firstOnly)
					.actionListener { _, component -> firstOnly = component.isSelected }
			}
			//firstOnlyOnFile
			row {
				checkBox(PlsBundle.message("inspection.script.general.tooManyExpression.option.firstOnlyOnFile"))
					.bindSelected(::firstOnlyOnFile)
					.actionListener { _, component -> firstOnlyOnFile = component.isSelected }
			}
		}
	}
}
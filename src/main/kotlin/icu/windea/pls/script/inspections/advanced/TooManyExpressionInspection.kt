package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

/**
 * 定义声明中过多的表达式的检查。
 */
class TooManyExpressionInspection: LocalInspectionTool() {
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
				doCheck(position, definitionMemberInfo)
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
				doCheck(position, definitionMemberInfo)
			}
			
			private fun doCheck(position: PsiElement, definitionMemberInfo: ParadoxDefinitionMemberInfo?) {
				if(definitionMemberInfo == null) return
				definitionMemberInfo.childPropertyOccurrenceMap.takeIf { it.isNotEmpty() }
					?.forEach { (configExpression, occurrence) ->
						doCheckOccurrence(occurrence, configExpression, position)
					}
				definitionMemberInfo.childValueOccurrenceMap.takeIf { it.isNotEmpty() }
					?.forEach { (configExpression, occurrence) ->
						doCheckOccurrence(occurrence, configExpression, position)
					}
			}
			
			private fun doCheckOccurrence(occurrence: Occurrence, configExpression: CwtDataExpression, position: PsiElement) {
				val (actual, _, max) = occurrence
				if(max != null && actual > max) {
					val isKey = configExpression is CwtKeyExpression
					val isConst = configExpression.type.isConstant()
					val description = if(isKey) {
						when {
							isConst -> PlsBundle.message("script.inspection.advanced.tooManyExpression.description.1.1", configExpression, max, actual)
							else -> PlsBundle.message("script.inspection.advanced.tooManyExpression.description.1.2", configExpression, max, actual)
						}
					} else {
						when {
							isConst -> PlsBundle.message("script.inspection.advanced.tooManyExpression.description.2.1", configExpression, max, actual)
							else -> PlsBundle.message("script.inspection.advanced.tooManyExpression.description.2.2", configExpression, max, actual)
						}
					}
					val highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
					holder.registerProblem(position, description, highlightType)
				}
			}
		})
		return holder.resultsArray
	}
}
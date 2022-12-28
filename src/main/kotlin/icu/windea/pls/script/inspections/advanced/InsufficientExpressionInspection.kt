package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 不充分的表达式的检查。
 */
class InsufficientExpressionInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptExpressionElement) visitExpressionElement(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			private fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
				ProgressManager.checkCanceled()
				if(element.text.isLeftQuoted()) return //忽略
				//得到完全匹配的CWT规则
				val config = ParadoxCwtConfigHandler.resolveConfigs(element, orDefault = false).firstOrNull() ?: return
				val configExpression = config.expression
				when(configExpression.type) {
					 CwtDataType.Int -> {
						val expression = element.expression ?: return
						val (min, max) = configExpression.extraValue<Tuple2<Int, Int?>>() ?: return
						val value = when {
							element is ParadoxScriptScriptedVariableReference -> element.referenceValue?.value ?: return //skip
							else -> element.value
						}.toIntOrNull() ?: return //skip
						if(!(min <= value && (max == null || max >= value))) {
							if(max != null) {
								holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.1", expression, min, max, value))
							} else {
								holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.2", expression, min, value))
							}
						}
					}
					 CwtDataType.Float -> {
						val expression = element.expression ?: return
						val (min, max) = configExpression.extraValue<Tuple2<Float, Float?>>() ?: return
						val value = when {
							element is ParadoxScriptScriptedVariableReference -> element.referenceValue?.value ?: return //skip
							else -> element.value
						}.toFloatOrNull() ?: return //skip
						if(!(min <= value && (max == null || max >= value))) {
							if(max != null) {
								holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.1", expression, min, max, value))
							} else {
								holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.2", expression, min, value))
							}
						}
					}
					CwtDataType.ColorField -> {
						val expression = element.expression ?: return
						if(element !is ParadoxScriptColor) return
						val expectedColorType = configExpression.value ?: return
						val colorType = element.colorType
						if(!(colorType == expectedColorType || (colorType == "rgb" && expectedColorType == "rgba"))) {
							holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.3", expression, expectedColorType, colorType))
						}
					}
					CwtDataType.Scope -> {
						//TODO
					}
					CwtDataType.ScopeField -> {
						//TODO
					}
					CwtDataType.IntValueField -> {
						//TODO
					}
					CwtDataType.IntVariableField -> {
						//TODO
					}
					else -> pass()
				}
			}
		})
		return holder.resultsArray
	}
}
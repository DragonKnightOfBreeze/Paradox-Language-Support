package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
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
				val text = element.text
				if(text.isLeftQuoted()) return //忽略
				//得到完全匹配的CWT规则
				val isKey = element is ParadoxScriptPropertyKey
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
						if(element !is ParadoxScriptStringExpressionElement) return
						val expectedScope = configExpression.value ?: return
						val textRange = TextRange.create(0, text.length)
						val configGroup = config.info.configGroup
						val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return
						val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
						val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
						val scopeContext = ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, parentScopeContext)
						if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return
						val expression = element.expression ?: return
						holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.4", expression, expectedScope, scopeContext.thisScope))
					}
					CwtDataType.ScopeGroup -> {
						if(element !is ParadoxScriptStringExpressionElement) return
						val expectedScopeGroup = configExpression.value ?: return
						val textRange = TextRange.create(0, text.length)
						val configGroup = config.info.configGroup
						val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return
						val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
						val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
						val scopeContext = ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, parentScopeContext)
						if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return
						val expression = element.expression ?: return
						holder.registerProblem(element, PlsBundle.message("script.inspection.advanced.insufficientExpression.description.5", expression, expectedScopeGroup, scopeContext.thisScope))
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
package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
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
				//得到完全匹配的CWT规则
				val isKey = element is ParadoxScriptPropertyKey
				val config = ParadoxCwtConfigHandler.resolveConfigs(element, orDefault = false).firstOrNull() ?: return
				val configExpression = config.expression
				val dataType = configExpression.type
				when {
					dataType == CwtDataType.Int -> {
						val expression = element.expression ?: return
						val (min, max) = configExpression.extraValue<Tuple2<Int, Int?>>() ?: return
						val value = element.intValue() ?: return
						if(!(min <= value && (max == null || max >= value))) {
							if(max != null) {
								holder.registerProblem(element, PlsBundle.message("inspection.script.general.insufficientExpression.description.1", expression, min, max, value))
							} else {
								holder.registerProblem(element, PlsBundle.message("inspection.script.general.insufficientExpression.description.2", expression, min, value))
							}
						}
					}
					dataType == CwtDataType.Float -> {
						val expression = element.expression ?: return
						val (min, max) = configExpression.extraValue<Tuple2<Float, Float?>>() ?: return
						val value = element.floatValue() ?: return
						if(!(min <= value && (max == null || max >= value))) {
							if(max != null) {
								holder.registerProblem(element, PlsBundle.message("inspection.script.general.insufficientExpression.description.1", expression, min, max, value))
							} else {
								holder.registerProblem(element, PlsBundle.message("inspection.script.general.insufficientExpression.description.2", expression, min, value))
							}
						}
					}
					dataType == CwtDataType.ColorField -> {
						val expression = element.expression ?: return
						if(element !is ParadoxScriptColor) return
						val expectedColorType = configExpression.value ?: return
						val colorType = element.colorType
						if(colorType == expectedColorType) return
						val message = PlsBundle.message("inspection.script.general.insufficientExpression.description.3", expression, expectedColorType, colorType)
						holder.registerProblem(element, message)
					}
					dataType.isFilePathType() -> {
						val expression = element.expression ?: return
						val fileExtensions = ParadoxFilePathHandler.getFileExtensionOptionValues(config)
						if(fileExtensions.isEmpty()) return
						val value = element.value
						if(fileExtensions.any { value.endsWith(it, true) }) return
						val extensionsString = fileExtensions.joinToString(" / ")
						val extension = value.substringAfterLast('.', "").lowercase().ifNotEmpty { ".$it" }
						val message = if(extension.isNotEmpty()) PlsBundle.message("inspection.script.general.insufficientExpression.description.4", expression, extensionsString, extension)
						else PlsBundle.message("inspection.script.general.insufficientExpression.description.4_1", expression, extensionsString)
						holder.registerProblem(element, message)
					}
					dataType == CwtDataType.Scope -> {
						if(element !is ParadoxScriptStringExpressionElement) return
						val expectedScope = configExpression.value ?: return
						val text = element.text
						val textRange = TextRange.create(0, text.length)
						val configGroup = config.info.configGroup
						val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return
						val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
						val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
						val scopeContext = ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, parentScopeContext)
						if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return
						val expression = element.expression ?: return
						val message = PlsBundle.message("inspection.script.general.insufficientExpression.description.5", expression, expectedScope, scopeContext.thisScope)
						holder.registerProblem(element, message)
					}
					dataType == CwtDataType.ScopeGroup -> {
						if(element !is ParadoxScriptStringExpressionElement) return
						val expectedScopeGroup = configExpression.value ?: return
						val text = element.text
						val textRange = TextRange.create(0, text.length)
						val configGroup = config.info.configGroup
						val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup, isKey) ?: return
						val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
						val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
						val scopeContext = ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, parentScopeContext)
						if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return
						val expression = element.expression ?: return
						val message = PlsBundle.message("inspection.script.general.insufficientExpression.description.6", expression, expectedScopeGroup, scopeContext.thisScope)
						holder.registerProblem(element, message)
					}
					dataType == CwtDataType.IntValueField -> {
						//TODO
					}
					dataType == CwtDataType.IntVariableField -> {
						//TODO
					}
					else -> pass()
				}
			}
		})
		return holder.resultsArray
	}
}
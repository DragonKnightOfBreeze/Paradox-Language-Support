package icu.windea.pls.script.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.psi.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor> {
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptProperty) visitScriptProperty(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			private fun visitScriptProperty(element: ParadoxScriptProperty) {
				val configs = ParadoxCwtConfigHandler.resolveConfigs(element)
				val config = configs.firstOrNull()
				if(config == null) return
				val definitionInfo by lazy { element.findParentDefinition()?.definitionInfo } 
				if(config is CwtPropertyConfig && config.expression.type == CwtDataType.ScopeField) {
					val resultScopeContext = ParadoxScopeHandler.getScopeContext(element)
					if(resultScopeContext == null) return
					val scopeFieldInfo = resultScopeContext.scopeFieldInfo
					if(scopeFieldInfo.isNullOrEmpty()) return
					val propertyKey = element.propertyKey
					for((scopeNode, scopeContext) in scopeFieldInfo) {
						val rangeInExpression = scopeNode.rangeInExpression
						when(scopeNode) {
							is ParadoxScopeLinkExpressionNode -> {
								val parentScopeContext = scopeContext.parent ?: continue
								val inputScopes = scopeNode.config.inputScopes
								if(!ParadoxScopeHandler.matchesScope(parentScopeContext, inputScopes)) {
									val description = PlsBundle.message("script.inspection.scope.incorrectScopeSwitch.description.1",
										scopeNode.text, inputScopes.joinToString(), parentScopeContext.thisScope)
									holder.registerProblem(propertyKey, rangeInExpression, description)
								}
							}
							//TODO 'event_target:xxx', not supported now
							is ParadoxScopeLinkFromDataExpressionNode -> {
								
							}
							//TODO may depends on usages
							//check when root parent scope context is not from event, scripted_trigger or scripted_effect
							is ParadoxSystemLinkExpressionNode -> {
								if(scopeContext.thisScope == ParadoxScopeHandler.anyScopeId) {
									val definitionType = definitionInfo?.type ?: continue
									if(config.info.configGroup.definitionTypesSkipCheckSystemLink.contains(definitionType)) continue
									val description = PlsBundle.message("script.inspection.scope.incorrectScopeSwitch.description.3",
										scopeNode.text)
									holder.registerProblem(propertyKey, rangeInExpression, description)
								}
							}
							//error
							is ParadoxErrorScopeExpressionNode -> break
						}
					}
				}
			}
		})
		return holder.resultsArray
	}
}

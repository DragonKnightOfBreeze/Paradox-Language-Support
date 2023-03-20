package icu.windea.pls.script.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class IncorrectScopeInspection: LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {
		override fun visitElement(element: PsiElement) {
			ProgressManager.checkCanceled()
			if(element is ParadoxScriptProperty) visitProperty(element)
			if(element is ParadoxScriptString) visitString(element)
		}
		
		private fun visitProperty(element: ParadoxScriptProperty) {
			val configs = ParadoxConfigHandler.getConfigs(element)
			val config = configs.firstOrNull() ?: return
			if(!ParadoxScopeHandler.isScopeContextSupported(element)) return
			val parentMember = ParadoxScopeHandler.findParentMember(element) ?: return
			val parentScopeContext = ParadoxScopeHandler.getScopeContext(parentMember) ?: return
			val supportedScopes = config.supportedScopes
			val configGroup = config.info.configGroup
			if(!ParadoxScopeHandler.matchesScope(parentScopeContext, supportedScopes, configGroup)) {
				val propertyKey = element.propertyKey
				val location = propertyKey
				val description = PlsBundle.message("inspection.script.scope.incorrectScope.description.1",
					propertyKey.expression, supportedScopes.joinToString(), parentScopeContext.scope.id)
				holder.registerProblem(location, description)
			}
		}
		
		private fun visitString(element: ParadoxScriptString) {
			val configs = ParadoxConfigHandler.getConfigs(element)
			val config = configs.firstOrNull() ?: return
			if(config.expression.type != CwtDataType.AliasKeysField) return
			val parentMember = ParadoxScopeHandler.findParentMember(element) ?: return
			val parentScopeContext = ParadoxScopeHandler.getScopeContext(parentMember) ?: return
			val supportedScopes = config.supportedScopes
			val configGroup = config.info.configGroup
			if(!ParadoxScopeHandler.matchesScope(parentScopeContext, supportedScopes, configGroup)) {
				val location = element
				val description = PlsBundle.message(
					"inspection.script.scope.incorrectScope.description.2",
					element.expression, supportedScopes.joinToString(), parentScopeContext.scope.id
				)
				holder.registerProblem(location, description)
			}
		}
	}
}


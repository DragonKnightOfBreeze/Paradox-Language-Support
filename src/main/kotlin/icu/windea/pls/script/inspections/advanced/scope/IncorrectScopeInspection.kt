package icu.windea.pls.script.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.script.psi.*

class IncorrectScopeInspection: LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : PsiElementVisitor() {
		override fun visitElement(element: PsiElement) {
			//这里只需要检查property，因为这背后应当是个别名（例如，alias[effect:xxx] = ...）
			if(element is ParadoxScriptProperty) visitMemberElement(element)
			if(element.isExpressionOrMemberContext()) super.visitElement(element)
		}
		
		private fun visitMemberElement(element: ParadoxScriptProperty) {
			val configs = ParadoxCwtConfigHandler.resolveConfigs(element)
			val config = configs.firstOrNull() ?: return
			if(!ParadoxScopeHandler.isScopeContextSupported(element)) return
			val parentMember = ParadoxScopeHandler.findParentMember(element) ?: return
			val parentScopeContext = ParadoxScopeHandler.getScopeContext(parentMember) ?: return
			val supportedScopes = config.supportedScopes
			val configGroup = config.info.configGroup
			if(!ParadoxScopeHandler.matchesScope(parentScopeContext, supportedScopes, configGroup)) {
				val propertyKey = element.propertyKey
				val location = propertyKey
				val description = PlsBundle.message("script.inspection.scope.incorrectScope.description.1",
					propertyKey.expression, supportedScopes.joinToString(), parentScopeContext.thisScope)
				holder.registerProblem(location, description)
			}
		}
	}
}


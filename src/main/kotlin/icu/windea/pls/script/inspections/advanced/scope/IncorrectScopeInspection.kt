package icu.windea.pls.script.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.script.psi.*

class IncorrectScopeInspection: LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				//这里只需要检查property，因为这背后应当是个别名（例如，alias[effect:xxx] = ...）
				if(element is ParadoxScriptProperty) visitMemberElement(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			private fun visitMemberElement(element: ParadoxScriptProperty) {
				val configs = ParadoxCwtConfigHandler.resolveConfigs(element)
				val config = configs.firstOrNull() ?: return
				if(!ParadoxScopeConfigHandler.isScopeContextSupported(element)) return
				val scopeContext = ParadoxScopeConfigHandler.getScopeContext(element, file) ?: return
				val supportedScopes = config.supportedScopes
				if(!ParadoxScopeConfigHandler.matchesScope(scopeContext, supportedScopes)) {
					val propertyKey = element.propertyKey
					val location = propertyKey
					val description = PlsBundle.message("script.inspection.scope.incorrectScope.description.1", propertyKey.expression, supportedScopes.joinToString(), scopeContext.thisScope)
					holder.registerProblem(location, description)
				}
			}
		})
		return holder.resultsArray
	}
}


package icu.windea.pls.localisation.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				//command also can inside property references and icons
				when(element) {
					is ParadoxLocalisationLocale -> return
					is ParadoxLocalisationCommandField -> return
					is ParadoxLocalisationCommandScope -> {
						visitLocalisationCommandScope(element)
						return
					}
					else -> super.visitElement(element)
				}
			}
			
			private fun visitLocalisationCommandScope(element: ParadoxLocalisationCommandScope) {
				val resolved = element.reference.resolve() ?: return
				when {
					//system scope or localisation scope
					resolved is CwtProperty -> {
						val config = resolved.getUserData(PlsKeys.cwtConfigKey)
						when{
							config is CwtLocalisationLinkConfig -> {
								val scopeContext = ParadoxScopeConfigHandler.getScopeContext(element, file) ?: return
								val supportedScopes = config.inputScopes
								if(!ParadoxScopeConfigHandler.matchesScope(scopeContext, supportedScopes)) {
									val description = PlsBundle.message("localisation.inspection.scope.incorrectScopeSwitch.description.1",
										element.name, supportedScopes.joinToString(), scopeContext.thisScope)
									holder.registerProblem(element, description)
								}
							}
							//TODO depends on usages, cannot check now
							//config is CwtSystemScopeConfig -> {
							//	val scopeContext = ParadoxScopeConfigHandler.getScopeContext(element, file) ?: return
							//	val resolvedScope = ParadoxScopeConfigHandler.resolveScopeBySystemScope(config, scopeContext)
							//	if(resolvedScope == null) {
							//		val location = element
							//		val description = PlsBundle.message("localisation.inspection.scope.incorrectScopeSwitch.description.3",
							//			element.name)
							//		holder.registerProblem(location, description)
							//	}
							//}
						}
					}
					//TODO event target or global event target - not supported yet
					resolved is ParadoxValueSetValueElement -> {
						return
					}
				}
			}
		})
		return holder.resultsArray
	}
}
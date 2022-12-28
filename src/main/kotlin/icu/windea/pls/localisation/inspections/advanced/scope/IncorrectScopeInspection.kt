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
import icu.windea.pls.script.psi.*

class IncorrectScopeInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				//command also can inside property references and icons
				when(element) {
					is ParadoxLocalisationLocale -> return
					is ParadoxLocalisationCommandField -> {
						visitLocalisationCommandField(element)
						return
					}
					is ParadoxLocalisationCommandScope -> return
					else -> super.visitElement(element)
				}
			}
			
			private fun visitLocalisationCommandField(element: ParadoxLocalisationCommandField) {
				val resolved = element.reference?.resolve() ?: return
				when {
					//predefined localisation command
					resolved is CwtProperty -> {
						val config = resolved.getUserData(PlsKeys.cwtConfigKey)
						if(config is CwtLocalisationCommandConfig) {
							val scopeContext = ScopeConfigHandler.getScopeContext(element, file) ?: return
							val supportedScopes = config.supportedScopes
							if(!ScopeConfigHandler.matchesScope(scopeContext, supportedScopes)) {
								val location = element
								val description = PlsBundle.message("localisation.inspection.scope.incorrectScope.description.1",
									element.name, supportedScopes.joinToString(), scopeContext.thisScope)
								holder.registerProblem(location, description)
							}
						}
					}
					//TODO scripted loc - any scope
					resolved is ParadoxScriptDefinitionElement -> {
						return 
					}
					//TODO variable - not supported yet
					resolved is ParadoxValueSetValueElement -> {
						return
					}
				}
			}
		})
		return holder.resultsArray
	}
}
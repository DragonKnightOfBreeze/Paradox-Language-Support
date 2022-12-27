package icu.windea.pls.localisation.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
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
				//currently only use "localisation links" here
				val gameType = selectGameType(element) ?: return
				val resolved = element.reference.resolve() ?: return
				when {
					//system scope or localisation scope
					resolved is CwtProperty -> {
						val config = getCwtConfig(project).getValue(gameType).localisationLinks[element.name] ?: return
						val scopeContext = ScopeConfigHandler.getScopeContext(element, file) ?: return
						val inputScopes = config.inputScopes
					}
					//event target or global event target - not supported yet
					resolved is ParadoxValueSetValueElement -> return //TODO
				}
				
			}
		})
		return holder.resultsArray
	}
}
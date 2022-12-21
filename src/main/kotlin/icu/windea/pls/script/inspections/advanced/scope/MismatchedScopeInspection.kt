package icu.windea.pls.script.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

class MismatchedScopeInspection: LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
		val gameType = selectGameType(file)
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : ParadoxScriptRecursiveElementWalkingVisitor() {
			
		})
		return holder.resultsArray
	}
}


package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的命令作用域的检查。
 */
class UnresolvedCommandScopeInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitCommandScope(element: ParadoxLocalisationCommandScope) {
			ProgressManager.checkCanceled()
			val location = element
			if(element.reference.resolved()) return
			val name = element.name
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unresolvedCommandScope.description", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
				ImportGameOrModDirectoryFix(location)
			)
		}
	}
}

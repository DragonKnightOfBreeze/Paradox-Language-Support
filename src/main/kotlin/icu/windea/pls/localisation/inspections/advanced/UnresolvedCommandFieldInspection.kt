package icu.windea.pls.localisation.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.references.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的命令字段的检查。
 */
class UnresolvedCommandFieldInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitCommandField(element: ParadoxLocalisationCommandField) {
			ProgressManager.checkCanceled()
			val location = element
			val reference = element.reference
			if(reference == null || reference.canResolve()) return
			val name = element.name
			holder.registerProblem(location, PlsBundle.message("inspection.localisation.advanced.unresolvedCommandField.description", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
				ImportGameOrModDirectoryFix(location)
			)
		}
	}
}

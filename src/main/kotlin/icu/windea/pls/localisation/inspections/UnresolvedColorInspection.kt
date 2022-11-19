package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的颜色的检查。
 */
class UnresolvedColorInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitColorfulText(element: ParadoxLocalisationColorfulText) {
			ProgressManager.checkCanceled()
			val location = element.colorId ?: return
			if(element.reference.canResolve()) return
			val name = element.name ?: return
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unresolvedColor.description", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
				ImportGameOrModDirectoryFix(location)
			)
		}
	}
}

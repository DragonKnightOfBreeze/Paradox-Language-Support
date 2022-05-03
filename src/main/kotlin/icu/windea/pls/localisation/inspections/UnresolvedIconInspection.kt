package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的图表的检查。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitIcon(element: ParadoxLocalisationIcon) {
			val resolved = element.reference?.resolve()
			if(resolved != null) return
			val location = element.iconId ?: return
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unresolvedIcon.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		}
	}
} 
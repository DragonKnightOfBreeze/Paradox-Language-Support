package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 不支持的颜色的检查。
 */
class UnsupportedColorInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitColorfulText(element: ParadoxLocalisationColorfulText) {
			val colorConfig = element.colorConfig
			if(colorConfig != null) return
			val location = element.colorId ?: return
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unsupportedColor.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		}
	}
}

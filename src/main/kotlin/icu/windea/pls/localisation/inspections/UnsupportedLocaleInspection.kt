package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 不支持的语言区域的检查。
 */
class UnsupportedLocaleInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitLocale(element: ParadoxLocalisationLocale) {
			val localeConfig = element.localeConfig
			if(localeConfig != null) return
			val location = element.localeId
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unsupportedLocale.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		}
	}
}


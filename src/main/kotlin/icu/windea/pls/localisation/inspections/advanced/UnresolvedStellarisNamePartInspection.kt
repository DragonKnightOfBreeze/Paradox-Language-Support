package icu.windea.pls.localisation.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

//icu.windea.pls.localisation.references.ParadoxLocalisationStellarisNamePartPsiReference

/**
 * 无法解析的Stellaris格式化引用的检查。
 */
class UnresolvedStellarisNamePartInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = object : ParadoxLocalisationVisitor() {
		override fun visitStellarisNamePart(element: ParadoxLocalisationStellarisNamePart) {
			val name = element.name ?: return
			val localisationProperty = element.parentOfType<ParadoxLocalisationProperty>() ?: return
			val localisationKey = localisationProperty.name
			val project = holder.project
			val valueSetName = StellarisNameFormatHandler.getValueSetName(localisationKey, project)
			if(valueSetName == null) {
				//do not report problems here
				//val message = PlsBundle.message("localisation.inspection.advanced.unresolvedStellarisNamePart.description.2", name, localisationKey)
				//holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
				return
			}
			val gameType = selectGameType(localisationProperty) ?: return
			val selector = valueSetValueSelector().gameType(gameType).declarationOnly()
			//必须要先有声明
			val declaration = ParadoxValueSetValueSearch.search(name, valueSetName, project, selector = selector).findFirst()
			if(declaration == null) {
				val message = PlsBundle.message("localisation.inspection.advanced.unresolvedStellarisNamePart.description.1", name, "value[$valueSetName]")
				holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
				return
			}
		}
	}
}
package icu.windea.pls.localisation.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class TooLongScopeLinkInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitCommand(element: ParadoxLocalisationCommand) {
			if(element.hasSyntaxError()) return //skip if any syntax error
			var firstScope: ParadoxLocalisationCommandScope? = null
			var lastScope: ParadoxLocalisationCommandScope? = null
			var size = 0
			element.processChild { 
				when {
					it is ParadoxLocalisationCommandScope -> {
						if(firstScope == null) firstScope = it
						lastScope = it
						size++
						true
					}
					it is ParadoxLocalisationCommandField -> false
					else -> true
				}
			}
			if(size > ParadoxScopeConfigHandler.maxScopeLinkSize) {
				val startOffset = firstScope?.textRangeInParent?.startOffset ?: return
				val endOffset = lastScope?.textRangeInParent?.endOffset ?: return
				val range = TextRange.create(startOffset, endOffset)
				val description = PlsBundle.message("localisation.inspection.scope.tooLongScopeLink.description")
				holder.registerProblem(element, range, description)
			}
		}
	}
}
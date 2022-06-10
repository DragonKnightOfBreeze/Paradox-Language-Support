package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.codeInsight.daemon.impl.quickfix.*
import com.intellij.psi.*
import icu.windea.pls.core.quickfix.InsertMissingTokenFix

/**
 * 快速修复一些语法错误.
 */
class ParadoxLocalisationErrorQuickFixProvider: ErrorQuickFixProvider {
	override fun registerErrorQuickFix(errorElement: PsiErrorElement, info: HighlightInfo) {
		val description = errorElement.errorDescription
		if(description == "XXX"){
			QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("£")) //ICON_END
		} else if(description == "XX"){
			QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("$")) //PROPERTY_REFERENCE_END
		}
	}
}
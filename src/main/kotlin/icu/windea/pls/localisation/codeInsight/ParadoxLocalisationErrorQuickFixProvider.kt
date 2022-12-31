package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.codeInsight.daemon.impl.quickfix.*
import com.intellij.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 快速修复一些语法错误。
 */
class ParadoxLocalisationErrorQuickFixProvider : ErrorQuickFixProvider {
	override fun registerErrorQuickFix(errorElement: PsiErrorElement, info: HighlightInfo) {
		if(errorElement.language != ParadoxLocalisationLanguage) return
		when {
			errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationIcon -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("£", errorElement.textRange.startOffset)) //ICON_END
			}
			errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationPropertyReference -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("$", errorElement.textRange.startOffset)) //PROPERTY_REFERENCE_END
			}
			errorElement.prevSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("\"", errorElement.textRange.endOffset)) //LEFT_QUOTE
			}
			errorElement.nextSibling == null && errorElement.parent is ParadoxLocalisationPropertyValue -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("\"", errorElement.textRange.startOffset)) //RIGHT_QUOTE
			}
		}
	}
}
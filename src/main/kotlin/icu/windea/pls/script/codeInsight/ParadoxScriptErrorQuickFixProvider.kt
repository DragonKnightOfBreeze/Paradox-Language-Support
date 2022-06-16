package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.codeInsight.daemon.impl.quickfix.*
import com.intellij.psi.*
import icu.windea.pls.core.quickfix.InsertMissingTokenFix
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 快速修复一些语法错误。
 */
class ParadoxScriptErrorQuickFixProvider : ErrorQuickFixProvider {
	override fun registerErrorQuickFix(errorElement: PsiErrorElement, info: HighlightInfo) {
		if(errorElement.language != ParadoxScriptLanguage) return
		when {
			errorElement.nextSibling == null && errorElement.parent is IParadoxScriptParameter -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("$", errorElement.textRange.startOffset)) //PARAMETER_END
			}
		}
	}
}
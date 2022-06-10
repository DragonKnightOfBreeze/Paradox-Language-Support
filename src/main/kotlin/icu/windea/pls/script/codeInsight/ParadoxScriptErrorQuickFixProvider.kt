package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.codeInsight.daemon.impl.quickfix.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.quickfix.InsertMissingTokenFix
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 快速修复一些语法错误。
 */
class ParadoxScriptErrorQuickFixProvider : ErrorQuickFixProvider {
	override fun registerErrorQuickFix(errorElement: PsiErrorElement, info: HighlightInfo) {
		if(errorElement.language != ParadoxScriptLanguage) return
		val prevElement = errorElement.prevSibling
		val prevType = prevElement.elementType
		when {
			(prevType == PARAMETER_ID || prevType == ARG_STRING_TOKEN || prevType == ARG_NUMBER_TOKEN) && errorElement.parent is IParadoxScriptParameter -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("$")) //PARAMETER_END
			}
		}
	}
}
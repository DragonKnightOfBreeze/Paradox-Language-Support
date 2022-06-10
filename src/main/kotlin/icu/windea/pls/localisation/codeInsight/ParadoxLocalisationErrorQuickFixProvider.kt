package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.impl.*
import com.intellij.codeInsight.daemon.impl.analysis.*
import com.intellij.codeInsight.daemon.impl.quickfix.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.quickfix.InsertMissingTokenFix
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyReference

/**
 * 快速修复一些语法错误。
 */
class ParadoxLocalisationErrorQuickFixProvider : ErrorQuickFixProvider {
	override fun registerErrorQuickFix(errorElement: PsiErrorElement, info: HighlightInfo) {
		if(errorElement.language != ParadoxLocalisationLanguage) return
		val prevElement = errorElement.prevSibling
		val prevType = prevElement.elementType
		when {
			(prevType == ICON_ID || prevType == ICON_FRAME) && errorElement.parent is ParadoxLocalisationIcon -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("£")) //ICON_END
			}
			//NOTE 目前版本下不会被匹配到，"$VALUE"会被直接识别为普通文本
			prevType == PROPERTY_REFERENCE_ID || prevType == PROPERTY_REFERENCE_PARAMETER_TOKEN && errorElement.parent is ParadoxLocalisationPropertyReference -> {
				QuickFixAction.registerQuickFixAction(info, InsertMissingTokenFix("$")) //PROPERTY_REFERENCE_END
			}
		}
	}
}
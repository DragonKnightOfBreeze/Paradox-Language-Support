package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.hint.DeclarationRangeHandler
import com.intellij.openapi.util.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 本地化（属性）的上下文信息（如：`l_english:`）。
 */
class ParadoxLocalisationDeclarationRangHandler: DeclarationRangeHandler<ParadoxLocalisationPropertyList> {
	override fun getDeclarationRange(container: ParadoxLocalisationPropertyList): TextRange? {
		return container.locale.textRange
	}
}
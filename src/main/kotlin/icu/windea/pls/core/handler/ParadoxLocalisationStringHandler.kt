package icu.windea.pls.core.handler

import com.intellij.lang.*
import com.intellij.psi.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

object ParadoxLocalisationStringHandler {
	@JvmStatic
	fun parseContents(element: ParadoxLocalisationString, chameleon: ASTNode): ASTNode {
		val gameType = ParadoxSelectorUtils.selectGameType(element)
		return when(gameType) {
			ParadoxGameType.Stellaris -> parseContentsForStellaris(element, chameleon) ?: chameleon
			else -> chameleon
		}
	}
	
	private fun parseContentsForStellaris(element: ParadoxLocalisationString, chameleon: ASTNode): ASTNode? {
		val property = element.parentOfType<ParadoxLocalisationProperty>() ?: return null
		val propertyName = property.name
		if(propertyName.startsWith("format.")) {
			//支持CWT规则类型`stellaris_name_format[xxx]`
			return parseStellarisFormatString(element, property, chameleon)
		} else {
			return null
		}
	}
	
	@WithGameType(ParadoxGameType.Stellaris)
	private fun parseStellarisFormatString(element: ParadoxLocalisationString, property: ParadoxLocalisationProperty, chameleon: ASTNode): ASTNode? {
		TODO()
	}
}
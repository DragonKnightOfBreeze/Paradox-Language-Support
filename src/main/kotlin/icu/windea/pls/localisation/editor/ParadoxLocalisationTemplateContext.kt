package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.template.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationTemplateContext : TemplateContextType(paradoxLocalisationId, paradoxLocalisationName) {
	override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
		val file = templateActionContext.file
		val startOffset = templateActionContext.startOffset
		val element = file.findElementAt(startOffset)
		val elementType = element.elementType
		return elementType == ParadoxLocalisationElementTypes.STRING_TOKEN //仅在string_token中提示
	}
}

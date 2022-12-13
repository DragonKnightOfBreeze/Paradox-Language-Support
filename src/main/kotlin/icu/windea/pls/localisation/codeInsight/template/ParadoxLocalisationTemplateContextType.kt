package icu.windea.pls.localisation.codeInsight.template

import com.intellij.codeInsight.template.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

abstract class ParadoxLocalisationTemplateContextType(presentableName: String) : TemplateContextType(presentableName) {
	final override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
		val file = templateActionContext.file
		if(!file.language.isKindOf(ParadoxLocalisationLanguage)) return false
		return doIsInContext(templateActionContext)
	}
	
	abstract fun doIsInContext(templateActionContext: TemplateActionContext): Boolean
	
	class Base: ParadoxLocalisationTemplateContextType(PlsBundle.message("localisation.templateContextType")) {
		override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
			return true
		}
	}
	
	class LocalisationText: ParadoxLocalisationTemplateContextType(PlsBundle.message("localisation.templateContextType.localisationText")) {
		override fun doIsInContext(templateActionContext: TemplateActionContext): Boolean {
			val file = templateActionContext.file
			val startOffset = templateActionContext.startOffset
			val endOffset = templateActionContext.endOffset
			return file.findElementAt(startOffset).elementType == STRING_TOKEN
				&& (file.findElementAt(endOffset).elementType == STRING_TOKEN || file.findElementAt(endOffset - 1).elementType == STRING_TOKEN)
		}
	}
}

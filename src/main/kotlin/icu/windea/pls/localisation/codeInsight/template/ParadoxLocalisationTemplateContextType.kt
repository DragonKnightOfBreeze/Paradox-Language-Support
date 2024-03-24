package icu.windea.pls.localisation.codeInsight.template

import com.intellij.codeInsight.template.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
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
			val start = file.findElementAt(startOffset) ?: return false
			if(start.elementType == LEFT_QUOTE) return false
			val startElement = start.parentOfType<ParadoxLocalisationPropertyValue>()
			return startElement != null
		}
	}
}

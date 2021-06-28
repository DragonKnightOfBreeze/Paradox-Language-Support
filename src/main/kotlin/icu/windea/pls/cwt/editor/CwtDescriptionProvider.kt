package icu.windea.pls.cwt.editor

import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

class CwtDescriptionProvider : ElementDescriptionProvider {
	companion object {
		private val _propertyDescription = message("cwt.description.property")
		private val _valueDescription = message("cwt.description.value")
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is CwtProperty -> if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
			is CwtString -> if(location == UsageViewTypeLocation.INSTANCE) _valueDescription else element.value
			else -> null
		}
	}
}
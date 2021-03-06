package icu.windea.pls.script.editor

import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDescriptionProvider : ElementDescriptionProvider {
	companion object {
		private val _variableDescription = message("paradox.script.description.variable")
		private val _propertyDescription = message("paradox.script.description.property")
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is ParadoxScriptVariable -> if(location == UsageViewTypeLocation.INSTANCE) _variableDescription else element.name
			is ParadoxScriptProperty -> if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
			else -> null
		}
	}
}

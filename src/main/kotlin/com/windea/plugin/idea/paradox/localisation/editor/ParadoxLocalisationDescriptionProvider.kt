package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.psi.*
import com.intellij.usageView.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationDescriptionProvider : ElementDescriptionProvider {
	companion object{
		private val _propertyDescription = message("paradox.localisation.description.property")
		private val _localeDescription = message("paradox.localisation.description.locale")
		private val _iconDescription = message("paradox.localisation.description.icon")
		private val _commandKeyDescription = message("paradox.localisation.description.commandKey")
		private val _serialNumberDescription = message("paradox.localisation.description.serialNumber")
		private val _colorfulTextDescription = message("paradox.localisation.description.colorfulText")
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is ParadoxLocalisationProperty -> if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
			is ParadoxLocalisationLocale -> if(location == UsageViewTypeLocation.INSTANCE) _localeDescription else element.name
			is ParadoxLocalisationIcon -> if(location == UsageViewTypeLocation.INSTANCE) _iconDescription else element.name
			is ParadoxLocalisationCommandKey -> if(location == UsageViewTypeLocation.INSTANCE) _commandKeyDescription else element.name
			is ParadoxLocalisationColorfulText -> if(location == UsageViewTypeLocation.INSTANCE) _serialNumberDescription else element.name
			is ParadoxLocalisationSerialNumber -> if(location == UsageViewTypeLocation.INSTANCE) _colorfulTextDescription else element.name
			else -> null
		}
	}
}

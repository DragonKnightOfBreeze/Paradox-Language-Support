package icu.windea.pls.localisation.editor

import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationDescriptionProvider : ElementDescriptionProvider {
	companion object{
		private val _propertyDescription = message("localisation.description.property")
		private val _localeDescription = message("localisation.description.locale")
		private val _iconDescription = message("localisation.description.icon")
		private val _sequentialNumberDescription = message("localisation.description.sequentialNumber")
		private val _commandScopeDescription = message("localisation.description.commandScope")
		private val _commandFieldDescription = message("localisation.description.commandField")
		private val _colorfulTextDescription = message("localisation.description.colorfulText")
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is ParadoxLocalisationProperty -> if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
			is ParadoxLocalisationLocale -> if(location == UsageViewTypeLocation.INSTANCE) _localeDescription else element.name
			is ParadoxLocalisationIcon -> if(location == UsageViewTypeLocation.INSTANCE) _iconDescription else element.name
			is ParadoxLocalisationSequentialNumber -> if(location == UsageViewTypeLocation.INSTANCE) _colorfulTextDescription else element.name
			is ParadoxLocalisationCommandScope -> if(location == UsageViewTypeLocation.INSTANCE) _commandScopeDescription else element.name
			is ParadoxLocalisationCommandField -> if(location == UsageViewTypeLocation.INSTANCE) _commandFieldDescription else element.name
			is ParadoxLocalisationColorfulText -> if(location == UsageViewTypeLocation.INSTANCE) _sequentialNumberDescription else element.name
			else -> null
		}
	}
}

package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
	companion object {
		private val _propertyDescription = PlsBundle.message("localisation.name.property")
		private val _localeDescription = PlsBundle.message("localisation.name.locale")
		private val _iconDescription = PlsBundle.message("localisation.name.icon")
		private val _sequentialNumberDescription = PlsBundle.message("localisation.name.sequentialNumber")
		private val _commandScopeDescription = PlsBundle.message("localisation.name.commandScope")
		private val _commandFieldDescription = PlsBundle.message("localisation.name.commandField")
		private val _colorfulTextDescription = PlsBundle.message("localisation.name.colorfulText")
		private val _localisationDescription = PlsBundle.message("localisation.name.localisation")
		private val _syncedLocalisationDescription = PlsBundle.message("localisation.name.syncedLocalisation")
	}
	
	override fun getType(element: PsiElement): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE)
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE)
	}
	
	override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
		return ElementDescriptionUtil.getElementDescription(element, UsageViewNodeTextLocation.INSTANCE)
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is ParadoxLocalisationProperty -> {
				//如果是本地化，需要特殊处理
				val localisationInfo = element.localisationInfo
				if(localisationInfo != null) {
					when(localisationInfo.category) {
						ParadoxLocalisationCategory.Localisation -> {
							if(location == UsageViewTypeLocation.INSTANCE) _localisationDescription else element.name
						}
						ParadoxLocalisationCategory.SyncedLocalisation -> {
							if(location == UsageViewTypeLocation.INSTANCE) _syncedLocalisationDescription else element.name
						}
					}
				} else {
					if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
				}
			}
			is ParadoxLocalisationLocale -> if(location == UsageViewTypeLocation.INSTANCE) _localeDescription else element.name
			is ParadoxLocalisationIcon -> if(location == UsageViewTypeLocation.INSTANCE) _iconDescription else element.name
			is ParadoxLocalisationSequentialNumber -> if(location == UsageViewTypeLocation.INSTANCE) _colorfulTextDescription else element.name
			is ParadoxLocalisationCommandScope -> if(location == UsageViewTypeLocation.INSTANCE) _commandScopeDescription else element.name
			is ParadoxLocalisationCommandField -> if(location == UsageViewTypeLocation.INSTANCE) _commandFieldDescription else element.name
			is ParadoxLocalisationColorfulText -> if(location == UsageViewTypeLocation.INSTANCE) _sequentialNumberDescription else element.name
			else -> null
		}
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return element is ParadoxLocalisationNamedElement
	}
	
	override fun getWordsScanner(): WordsScanner {
		return ParadoxLocalisationWordScanner()
	}
}

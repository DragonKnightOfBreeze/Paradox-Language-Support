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
							if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.localisation") else element.name
						}
						ParadoxLocalisationCategory.SyncedLocalisation -> {
							if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.syncedLocalisation") else element.name
						}
					}
				} else {
					if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.property") else element.name
				}
			}
			is ParadoxLocalisationLocale -> if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.locale") else element.name
			is ParadoxLocalisationIcon -> if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.icon") else element.name
			is ParadoxLocalisationSequentialNumber -> if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.colorfulText") else element.name
			is ParadoxLocalisationCommandScope -> if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.commandScope") else element.name
			is ParadoxLocalisationCommandField -> if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.commandField") else element.name
			is ParadoxLocalisationColorfulText -> if(location == UsageViewTypeLocation.INSTANCE) PlsBundle.message("localisation.name.sequentialNumber") else element.name
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

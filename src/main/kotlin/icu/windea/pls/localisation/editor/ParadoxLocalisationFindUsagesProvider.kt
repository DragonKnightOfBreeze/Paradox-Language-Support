package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
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
				when(location) {
					UsageViewTypeLocation.INSTANCE -> {
						val localisationInfo = element.localisationInfo
						when(localisationInfo?.category) {
							ParadoxLocalisationCategory.Localisation -> PlsBundle.message("localisation.name.localisation")
							ParadoxLocalisationCategory.SyncedLocalisation -> PlsBundle.message("localisation.name.syncedLocalisation")
							else -> PlsBundle.message("localisation.name.property")
						}
					}
					else -> element.name
				}
			}
			is ParadoxLocalisationLocale -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("localisation.name.locale")
					else -> element.name
				}
			}
			is ParadoxParameterElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.parameter")
					else -> element.name
				}
			}
			is ParadoxValueSetValueElement -> {
				when(location) {
					UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.valueSetValue")
					UsageViewNodeTextLocation.INSTANCE -> element.name + ": " + element.valueSetName
					else -> element.name
				}
			}
			else -> null
		}
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return when(element){
			is ParadoxLocalisationProperty -> true
			is ParadoxLocalisationLocale -> true
			is ParadoxParameterElement -> true
			is ParadoxValueSetValueElement -> true
			else -> false
		} 
	}
	
	override fun getWordsScanner(): WordsScanner {
		return ParadoxLocalisationWordScanner()
	}
}

package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*

class CwtFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
	override fun getType(element: PsiElement): String {
		return getElementDescription(element, UsageViewTypeLocation.INSTANCE).orEmpty()
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return getElementDescription(element, UsageViewLongNameLocation.INSTANCE).orEmpty()
	}
	
	override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
		return getElementDescription(element, UsageViewNodeTextLocation.INSTANCE).orEmpty()
	}
	
	override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
		return when(element) {
			is CwtProperty -> {
				val configType = CwtConfigType.resolve(element)?.takeIf { it.isReference } ?: return null
				when(location) {
					UsageViewTypeLocation.INSTANCE -> configType.descriptionText
					else -> element.name
				}
			}
			is CwtString -> {
				val configType = CwtConfigType.resolve(element)?.takeIf { it.isReference } ?: return null
				when(location) {
					UsageViewTypeLocation.INSTANCE -> configType.descriptionText
					else -> element.value
				}
			}
			else -> null
		}
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return when(element) {
			is CwtProperty -> CwtConfigType.resolve(element)?.isReference == true
			is CwtString -> CwtConfigType.resolve(element)?.isReference == true
			else -> false
		}
	}
	
	override fun getWordsScanner(): WordsScanner {
		return CwtWordScanner()
	}
}

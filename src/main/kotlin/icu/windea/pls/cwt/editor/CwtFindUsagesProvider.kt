package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

class CwtFindUsagesProvider : FindUsagesProvider , ElementDescriptionProvider{
	companion object {
		val _propertyDescription = PlsBundle.message("cwt.description.property")
		val _valueDescription = PlsBundle.message("cwt.description.value")
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
			is CwtProperty -> if(location == UsageViewTypeLocation.INSTANCE) _propertyDescription else element.name
			is CwtString -> if(location == UsageViewTypeLocation.INSTANCE) _valueDescription else element.value
			else -> null
		}
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return false //NOTE 总是不支持：没有必要，并且引用可能过多
	}
	
	override fun getWordsScanner(): WordsScanner {
		return CwtWordScanner()
	}
}

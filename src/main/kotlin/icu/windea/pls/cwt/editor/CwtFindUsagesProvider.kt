package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

class CwtFindUsagesProvider : FindUsagesProvider {
	companion object {
		val _propertyName = message("cwt.name.property")
		val _valueName = message("cwt.name.value")
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return if(element is CwtNamedElement) element.name.orEmpty() else ""
	}
	
	override fun getType(element: PsiElement): String {
		return when(element) {
			is CwtProperty -> _propertyName
			is CwtString -> _valueName
			else -> ""
		}
	}
	
	override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
		return getDescriptiveName(element)
	}
	
	override fun getHelpId(psiElement: PsiElement): String {
		return HelpID.FIND_OTHER_USAGES
	}
	
	override fun canFindUsagesFor(element: PsiElement): Boolean {
		return element is CwtNamedElement
	}
	
	override fun getWordsScanner(): WordsScanner {
		return CwtWordScanner()
	}
}

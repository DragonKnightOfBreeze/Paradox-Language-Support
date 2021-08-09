package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFindUsagesProvider : FindUsagesProvider {
	companion object{
		val _variableName = message("script.name.variable")
		val _propertyName = message("script.name.property")
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return if(element is ParadoxScriptNamedElement) element.name.orEmpty() else ""
	}

	override fun getType(element: PsiElement): String {
		return when(element) {
			is ParadoxScriptVariable -> _variableName
			is ParadoxScriptProperty -> _propertyName
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
		return element is ParadoxScriptNamedElement
	}

	override fun getWordsScanner(): WordsScanner {
		return ParadoxScriptWordScanner()
	}
}

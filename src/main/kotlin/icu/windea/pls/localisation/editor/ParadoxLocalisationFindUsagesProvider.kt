package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationFindUsagesProvider : FindUsagesProvider {
	companion object{
		private val _propertyName = message("localisation.name.property")
		private val _localeName = message("localisation.name.locale")
		private val _iconName = message("localisation.name.icon")
		private val _sequentialNumberName = message("localisation.name.sequentialNumber")
		private val _commandScopeName = message("localisation.name.commandScope")
		private val _commandFieldName = message("localisation.name.commandField")
		private val _colorfulTextName = message("localisation.name.colorfulText")
	}
	
	override fun getDescriptiveName(element: PsiElement): String {
		return if(element is PsiNamedElement) element.name.orEmpty() else ""
	}

	override fun getType(element: PsiElement): String {
		return when(element) {
			is ParadoxLocalisationProperty -> _propertyName
			is ParadoxLocalisationLocale -> _localeName
			is ParadoxLocalisationIcon ->_iconName
			is ParadoxLocalisationSequentialNumber -> _colorfulTextName
			is ParadoxLocalisationCommandScope -> _commandScopeName
			is ParadoxLocalisationCommandField -> _commandFieldName
			is ParadoxLocalisationColorfulText -> _sequentialNumberName
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
		return element is ParadoxLocalisationNamedElement
	}

	override fun getWordsScanner(): WordsScanner {
		return ParadoxLocalisationWordScanner()
	}
}

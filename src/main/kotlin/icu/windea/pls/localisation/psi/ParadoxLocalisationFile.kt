package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.structureView.*
import icu.windea.pls.script.structureView.*

class ParadoxLocalisationFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage) {
	override fun getFileType() = ParadoxLocalisationFileType

	val propertyLists: List<ParadoxLocalisationPropertyList>
		get() = findChildrenByClass(ParadoxLocalisationPropertyList::class.java).toList()
	
	val locale: ParadoxLocalisationLocale?
		get() = propertyLists.firstOrNull()?.locale
	
	val properties: List<ParadoxLocalisationProperty>
		get() = propertyLists.firstOrNull()?.propertyList ?: emptyList()
	
	val localeIdFromFileName get() = "l_" + name.substringBeforeLast('.').substringAfterLast("l_")
}

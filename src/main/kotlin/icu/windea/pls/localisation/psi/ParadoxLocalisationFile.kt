package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*

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
	
	fun getLocaleIdFromFileName(): String?  {
		if(!name.endsWith(".yml", true)) return null
		val dotIndex = name.lastIndexOf('.').let { if(it == -1) name.lastIndex else it }
		val prefixIndex = name.lastIndexOf("l_", dotIndex)
		if(prefixIndex == -1) return null
		return name.substring(prefixIndex + 2, name.length - 4)
	}
	
	fun getExpectedFileName(localeId: String): String  {
		val dotIndex = name.lastIndexOf('.').let { if(it == -1) name.lastIndex else it }
		val prefixIndex = name.lastIndexOf("l_", dotIndex)
		if(prefixIndex == -1) {
			return name.substring(0, dotIndex) + localeId + ".yml"
		} else {
			return name.substring(0, prefixIndex) + localeId + ".yml" 
		}
	}
}

package icu.windea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.structureView.*
import icu.windea.pls.script.structureView.*

class ParadoxLocalisationFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage){
	override fun getFileType() = ParadoxLocalisationFileType
	
	override fun getPresentation(): ItemPresentation {
		return ParadoxLocalisationFileTreeElement(this)
	}
	
	val locale: ParadoxLocalisationLocale?
		get() = findChildByClass(ParadoxLocalisationLocale::class.java)
	
	val properties: List<ParadoxLocalisationProperty>
		get() = findChildrenByClass(ParadoxLocalisationProperty::class.java).toList()
}

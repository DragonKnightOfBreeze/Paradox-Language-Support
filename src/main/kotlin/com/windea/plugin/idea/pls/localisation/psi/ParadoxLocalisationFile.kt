package com.windea.plugin.idea.pls.localisation.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.localisation.*

class ParadoxLocalisationFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxLocalisationLanguage){
	override fun getFileType() = ParadoxLocalisationFileType
	
	val locale: ParadoxLocalisationLocale?
		get() = findChildByClass(ParadoxLocalisationLocale::class.java)
	
	val properties: List<ParadoxLocalisationProperty>
		get() = findChildrenByClass(ParadoxLocalisationProperty::class.java).toList()
}

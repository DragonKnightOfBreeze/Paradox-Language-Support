package com.windea.plugin.idea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.script.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage) {
	override fun getFileType() = ParadoxScriptFileType

	val rootBlock get() = findChildByClass(ParadoxScriptRootBlock::class.java)
	
	val variables get() = rootBlock?.variableList?:listOf()
	
	val properties get() =  rootBlock?.propertyList?:listOf()

	val values get() = rootBlock?.valueList?:listOf()
}

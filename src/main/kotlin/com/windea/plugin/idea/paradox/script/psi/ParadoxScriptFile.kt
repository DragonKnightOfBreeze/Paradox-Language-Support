package com.windea.plugin.idea.paradox.script.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage) {
	override fun getFileType() = ParadoxScriptFileType

	val rootBlock get() = findChildByClass(ParadoxScriptRootBlock::class.java)
	
	val variables get() = rootBlock?.variableList?:listOf()
	
	val properties get() =  rootBlock?.propertyList?:listOf()

	val values get() = rootBlock?.valueList?:listOf()
	
	val fileInfo = getFileInfo(this)
}

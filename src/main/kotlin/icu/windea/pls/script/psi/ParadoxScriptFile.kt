package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import icu.windea.pls.script.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage) {
	override fun getFileType() = ParadoxScriptFileType

	val rootBlock get() = findChildByClass(ParadoxScriptRootBlock::class.java)
	
	val variables get() = rootBlock?.variableList?:emptyList()
	
	val properties get() =  rootBlock?.propertyList?:emptyList()

	val values get() = rootBlock?.valueList?:emptyList()
}

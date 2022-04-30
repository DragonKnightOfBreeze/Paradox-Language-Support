package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxDefinitionProperty {
	override fun getFileType() = ParadoxScriptFileType
	
	override fun getStub(): ParadoxScriptFileStub? {
		return super.getStub().cast()
	}
	
	override val block get() = findChildByClass(ParadoxScriptRootBlock::class.java)
	
	val variables get() = block?.variableList.orEmpty()
}


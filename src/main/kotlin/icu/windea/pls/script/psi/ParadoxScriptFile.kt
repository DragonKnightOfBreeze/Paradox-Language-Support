package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.script.structureView.*
import javax.swing.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxDefinitionProperty {
	override fun getFileType() = ParadoxScriptFileType
	
	override fun getStub(): ParadoxScriptFileStub? {
		return super.getStub().cast()
	}
	
	override val pathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	
	override val originalPathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	
	override val block get() = findOptionalChild<ParadoxScriptRootBlock>()
	
	val variables get() = block?.variableList.orEmpty()
}


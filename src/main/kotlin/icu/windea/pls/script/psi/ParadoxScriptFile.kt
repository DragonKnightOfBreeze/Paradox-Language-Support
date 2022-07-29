package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.navigation.*

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
	
	override fun subtreeChanged() {
		clearDefinitionElementInfo() //清除其中的定义元素信息
	}
	
	override fun getPresentation(): ItemPresentation {
		return ParadoxScriptFilePresentation(this)
	}
}


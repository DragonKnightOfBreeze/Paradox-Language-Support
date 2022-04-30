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
	
	override fun getPresentation(): ItemPresentation {
		return ParadoxScriptFileTreeElement(this)
	}
	
	override fun getIcon(flags: Int): Icon? {
		//如果文件名是descriptor.mod（不区分大小写），这里仍然要显示脚本文件的图标
		if(definitionInfo != null && !name.equals(descriptorFileName, true)) return PlsIcons.definitionIcon
		return super.getIcon(flags)
	}
	
	override val pathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	
	override val originalPathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	
	override val block get() = findChildByClass(ParadoxScriptRootBlock::class.java)
	
	val variables get() = block?.variableList.orEmpty()
}


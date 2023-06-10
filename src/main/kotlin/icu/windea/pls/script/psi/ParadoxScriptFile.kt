package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.navigation.*
import javax.swing.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxScriptDefinitionElement {
	override val block get() = findChild<ParadoxScriptRootBlock>()
	
	override fun getIcon(flags: Int): Icon? {
		//对模组描述符文件使用特定的图标
		if(name.equals(PlsConstants.descriptorFileName, true)) return PlsIcons.DescriptorFile
		return super.getIcon(flags)
	}
	
	override fun getFileType() = ParadoxScriptFileType
	
	override fun getStub(): ParadoxScriptFileStub? {
		return super.getStub().castOrNull()
	}
	
	override fun getPresentation(): ItemPresentation {
		return ParadoxScriptFilePresentation(this)
	}
	
	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return super.isEquivalentTo(another) || (another is ParadoxScriptFile && fileInfo == another.fileInfo)
	}
}


package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.navigation.*
import icu.windea.pls.script.psi.impl.*
import javax.swing.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxScriptDefinitionElement {
	@Volatile private var _parameterMap: Map<String, List<Tuple2<SmartPsiElementPointer<ParadoxParameter>, String?>>>? = null
	
	override val pathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	override val originalPathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	override val parameterMap: Map<String, List<Tuple2<SmartPsiElementPointer<ParadoxParameter>, String?>>>
		get() = _parameterMap ?: doGetParameters().also { _parameterMap = it }
	
	override val block get() = findChild<ParadoxScriptRootBlock>()
	
	private fun doGetParameters(): Map<String, List<Tuple2<SmartPsiElementPointer<ParadoxParameter>, String?>>> {
		return ParadoxScriptPsiImplUtil.getParameterMap(this)
	}
	
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
	
	override fun subtreeChanged() {
		_parameterMap = null
		super.subtreeChanged()
	}
}


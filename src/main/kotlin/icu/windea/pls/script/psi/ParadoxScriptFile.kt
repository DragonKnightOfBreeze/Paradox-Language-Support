package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.navigation.*
import javax.swing.*

class ParadoxScriptFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxDefinitionProperty {
	override val pathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	
	override val originalPathName get() = name.let { name -> name.substringBeforeLast(".", name) }
	
	override val block get() = findOptionalChild<ParadoxScriptRootBlock>()
	
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
	
	//@Volatile private var _valueSetValueMap: Map<String, Set<SmartPsiElementPointer<ParadoxExpressionElement>>>? = null
	
	//val valueSetValueMap: Map<String, Set<SmartPsiElementPointer<ParadoxExpressionElement>>>
	//	get() = _valueSetValueMap ?: ParadoxScriptPsiImplUtil.getValueSetValueMap(this).also { _valueSetValueMap = it }
	
	override fun subtreeChanged() {
		//_valueSetValueMap = null
		clearCachedData()
		super.subtreeChanged()
	}
	
	private fun clearCachedData() {
		//当脚本文件内容发生更改时，需要重置所有来自typeComment的definitionInfo以及下面的definitionElementInfo
		accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptProperty || element is ParadoxScriptValue || element is ParadoxScriptPropertyValue) {
					if(element is ParadoxScriptProperty) {
						if(element.getUserData(PlsKeys.cachedDefinitionInfoKey)?.value?.shouldIndex == true) {
							element.putUserData(PlsKeys.cachedDefinitionInfoKey, null)
						}
					}
					if(element is ParadoxScriptProperty) {
						if(element.getUserData(PlsKeys.definitionElementInfoKey)?.definitionInfo?.shouldIndex == true) {
							element.putUserData(PlsKeys.definitionElementInfoKey, null)
						}
					} else if(element is ParadoxScriptValue) {
						if(element.getUserData(PlsKeys.definitionElementInfoKey)?.definitionInfo?.shouldIndex == true) {
							element.putUserData(PlsKeys.definitionElementInfoKey, null)
						}
					}
					super.visitElement(element)
				}
			}
		})
	}
}


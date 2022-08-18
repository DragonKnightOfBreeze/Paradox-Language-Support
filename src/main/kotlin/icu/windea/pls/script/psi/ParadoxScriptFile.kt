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
	
	override fun getPresentation(): ItemPresentation {
		return ParadoxScriptFilePresentation(this)
	}
	
	override fun subtreeChanged() {
		clearCachedData()
		super.subtreeChanged()
	}
	
	private fun clearCachedData() {
		//当脚本文件内容发生更改时，需要重置所有来自typeComment的definitionInfo以及下面的definitionElementInfo
		accept(object : PsiRecursiveElementVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptProperty || element is ParadoxScriptValue || element is ParadoxScriptPropertyValue) {
					if(element is ParadoxScriptProperty) {
						if(element.getUserData(PlsKeys.cachedDefinitionInfoKey)?.value?.fromTypeComment == true) {
							element.putUserData(PlsKeys.cachedDefinitionInfoKey, null)
						}
					}
					if(element is ParadoxScriptProperty) {
						if(element.getUserData(PlsKeys.definitionElementInfoKey)?.definitionInfo?.fromTypeComment == true) {
							element.putUserData(PlsKeys.definitionElementInfoKey, null)
						}
					} else if(element is ParadoxScriptValue) {
						if(element.getUserData(PlsKeys.definitionElementInfoKey)?.definitionInfo?.fromTypeComment == true) {
							element.putUserData(PlsKeys.definitionElementInfoKey, null)
						}
					}
					super.visitElement(element)
				}
			}
		})
	}
}


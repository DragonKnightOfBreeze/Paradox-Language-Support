package com.windea.plugin.idea.paradox.core.reference

import com.intellij.openapi.paths.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.xml.util.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxDefinitionPathReference
import com.windea.plugin.idea.paradox.script.psi.*

//用于兼容markdown锚点
//同样需要兼容markdown navigator的markdown锚点

class ParadoxPathReferenceProvider: PathReferenceProvider {
	override fun createReferences(psiElement: PsiElement, references: MutableList<PsiReference>, soft: Boolean): Boolean {
		val size = references.size
		if(size <= 2) return false
		val fileReference = references[size-2]
		if(fileReference !is PsiFileReference) return false
		if(fileReference.canonicalText.isEmpty()) return false //排除自身文件
		val file = fileReference.resolve()?:return false
		when{
			file is ParadoxScriptFile -> {
				val anchorReference = references[size-1]
				if(anchorReference !is AnchorReference) return false
				val anchorText = anchorReference.canonicalText
				if(anchorText.isEmpty()) return false //排除空锚点
				val newReference = ParadoxDefinitionPathReference(anchorReference.element,anchorReference.rangeInElement,anchorText,fileReference)
				references.dropLast(1)
				references.add(newReference)
			}
			file is ParadoxLocalisationFile -> {
				val anchorReference = references[size-1]
				if(anchorReference !is AnchorReference) return false
				val anchorText = anchorReference.canonicalText
				if(anchorText.isEmpty()) return false //排除空锚点
				val newReference = ParadoxDefinitionPathReference(anchorReference.element, anchorReference.rangeInElement, anchorText, fileReference)
				references.dropLast(1)
				references.add(newReference)
			}
		}
		return false 
	}
	
	override fun getPathReference(path: String, element: PsiElement): PathReference? {
		return null
	}
}
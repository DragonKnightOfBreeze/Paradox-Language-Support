package icu.windea.pls.core.reference

import com.intellij.openapi.paths.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.xml.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.reference.*
import icu.windea.pls.script.psi.*

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
				references.removeAt(size-1)
				references.add(newReference)
				return true
			}
			file is ParadoxLocalisationFile -> {
				val anchorReference = references[size-1]
				if(anchorReference !is AnchorReference) return false
				val anchorText = anchorReference.canonicalText
				if(anchorText.isEmpty()) return false //排除空锚点
				val newReference = ParadoxDefinitionPathReference(anchorReference.element, anchorReference.rangeInElement, anchorText, fileReference)
				references.removeAt(size-1)
				references.add(newReference)
				return true
			}
		}
		return false 
	}
	
	override fun getPathReference(path: String, element: PsiElement): PathReference? {
		return null
	}
}
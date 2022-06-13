package icu.windea.pls.core

import com.intellij.openapi.paths.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.xml.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.reference.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.reference.*

//用于兼容markdown锚点

//com.intellij.xml.util.AnchorPathReferenceProvider

class ParadoxPathReferenceProvider : PathReferenceProvider {
	override fun createReferences(psiElement: PsiElement, references: MutableList<PsiReference>, soft: Boolean): Boolean {
		val size = references.size
		if(size <= 2) return false
		val fileReference = references[size - 2]
		if(fileReference !is PsiFileReference) return false
		if(fileReference.canonicalText.isEmpty()) return false //排除自身文件
		val file = fileReference.resolve() ?: return false
		when {
			file is ParadoxScriptFile -> {
				//得到definition的引用
				val anchorReference = references[size - 1]
				if(anchorReference !is AnchorReference) return false
				val anchorText = anchorReference.canonicalText
				if(anchorText.isEmpty()) return false //排除空锚点
				val element = anchorReference.element
				val rangeInElement = anchorReference.rangeInElement
				val newReference = ParadoxDefinitionAnchorReference(element, rangeInElement, anchorText, file)
				references.removeAt(size - 1)
				references.add(newReference)
				return true
			}
			file is ParadoxLocalisationFile -> {
				//得到localisation或localisation_synced的引用
				val category = ParadoxLocalisationCategory.resolve(file) ?: return false
				val anchorReference = references[size - 1]
				if(anchorReference !is AnchorReference) return false
				val anchorText = anchorReference.canonicalText
				if(anchorText.isEmpty()) return false //排除空锚点
				val element = anchorReference.element
				val rangeInElement = anchorReference.rangeInElement
				val newReference = ParadoxLocalisationAnchorReference(element, rangeInElement, anchorText, file, category)
				references.removeAt(size - 1)
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
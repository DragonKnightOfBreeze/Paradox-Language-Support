package com.windea.plugin.idea.pls.localisation.reference

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.search.*
import com.intellij.xml.util.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ParadoxDefinitionPathReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val anchor:String,
	private val fileReference: PsiFileReference
): PsiReferenceBase<PsiElement>(element,rangeInElement), AnchorReference, EmptyResolveMessageProvider {
	override fun resolve(): PsiElement? {
		val file = getFile() ?: return null
		return findLocalisation(anchor,null,element.project, GlobalSearchScope.fileScope(file),hasDefault=true)
	}
	
	override fun getUnresolvedMessagePattern(): String {
		val file = getFile() ?: return message("paradox.localisation.annotator.unresolvedLocalisation", anchor)
		return message("paradox.localisation.annotator.unresolvedLocalisationInFile", anchor, file.name)
	}
	
	private fun getFile(): ParadoxLocalisationFile? {
		return fileReference.resolve() as? ParadoxLocalisationFile
	}
}
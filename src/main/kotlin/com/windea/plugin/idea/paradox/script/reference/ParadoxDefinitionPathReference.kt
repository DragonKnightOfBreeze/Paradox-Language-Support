package com.windea.plugin.idea.paradox.script.reference

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.search.*
import com.intellij.xml.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxDefinitionPathReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val anchor:String,
	private val fileReference: PsiFileReference
): PsiReferenceBase<PsiElement>(element,rangeInElement), AnchorReference, EmptyResolveMessageProvider {
	override fun resolve(): PsiElement? {
		val file = getFile() ?: return null
		return findDefinition(anchor,null,element.project, GlobalSearchScope.fileScope(file))
	}
	
	override fun getUnresolvedMessagePattern(): String {
		val file = getFile() ?: return message("paradox.script.annotator.unresolvedDefinition", anchor)
		return message("paradox.script.annotator.unresolvedDefinitionInFile", anchor, file.name)
	}
	
	private fun getFile(): ParadoxScriptFile? {
		return fileReference.resolve() as? ParadoxScriptFile
	}
}
package icu.windea.pls.script.reference

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.xml.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionPathReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val anchor:String,
	private val file: ParadoxScriptFile
): PsiReferenceBase<PsiElement>(element,rangeInElement), AnchorReference, EmptyResolveMessageProvider {
	override fun resolve(): PsiElement? {
		return findDefinition(anchor,null,element.project, GlobalSearchScope.fileScope(file))
	}
	
	override fun getUnresolvedMessagePattern(): String {
		return message("script.annotator.unresolvedDefinitionInFile", anchor, file.name)
	}
	
	private fun getFile(): ParadoxScriptFile {
		return file
	}
}
package icu.windea.pls.script.reference

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.xml.util.*
import icu.windea.pls.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.core.selector.*

class ParadoxDefinitionPathReference(
	element: PsiElement,
	rangeInElement: TextRange,
	private val anchor: String,
	private val file: ParadoxScriptFile
) : PsiReferenceBase<PsiElement>(element, rangeInElement), AnchorReference, EmptyResolveMessageProvider {
	override fun resolve(): PsiElement? {
		return findDefinition(anchor, null, element.project, GlobalSearchScope.fileScope(file), selector = definitionSelector().gameTypeFrom(element))
	}
	
	override fun getUnresolvedMessagePattern(): String {
		return PlsBundle.message("script.annotator.unresolvedDefinitionInFile", anchor, file.name)
	}
	
	private fun getFile(): ParadoxScriptFile {
		return file
	}
}
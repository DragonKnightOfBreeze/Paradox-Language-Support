package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptLinkValueReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
): PsiReferenceBase<PsiElement>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException() //TODO
	}
	
	override fun resolve(): PsiElement? {
		val gameType = element.fileInfo?.gameType ?: return null
		val name = rangeInElement.substring(element.text)
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		return CwtConfigHandler.resolveLinkValue(name, configGroup)
	}
}
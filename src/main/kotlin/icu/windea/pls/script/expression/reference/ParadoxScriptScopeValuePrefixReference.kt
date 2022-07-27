package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptScopeValuePrefixReference(
	element: PsiElement,
	rangeInElement: TextRange
): PsiReferenceBase<PsiElement>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun resolve(): PsiElement? {
		val gameType = element.fileInfo?.gameType ?: return null
		val name = rangeInElement.substring(element.text)
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		return CwtConfigHandler.resolveLinkValuePrefix(name, configGroup)
	}
}
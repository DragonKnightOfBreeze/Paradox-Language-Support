package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueFieldValueReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	private val resolved: PsiElement?
) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun resolve(): PsiElement? {
		if(resolved != null) return resolved
		
		//val name = rangeInElement.substring(element.text)
		//val gameType = element.fileInfo?.gameType ?: return null
		//val configGroup = getCwtConfig(element.project).getValue(gameType)
		//return CwtConfigHandler.resolveValueFieldValue(name, configGroup)
		return null
	}
}
package icu.windea.pls.script.surroundWith

import com.intellij.lang.surroundWith.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

/**
 * 从句的包围器，将选中的表达式（一个或多个属性或者单独的值）用花括号包围并在前面加上属性的键。
 * 
 * ```
 * # 应用前：
 * k = v
 * 
 * # 应用后：
 * key = {
 *     k = v
 * }
 * ```
 */
class ParadoxScriptClausePropertySurrounder: Surrounder {
	@Suppress("DialogTitleCapitalization")
	override fun getTemplateDescription(): String {
		return PlsBundle.message("cwt.surroundWith.clauseProperty.description")
	}
	
	override fun isApplicable(elements: Array<out PsiElement>): Boolean {
		return true
	}
	
	override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
		if(elements.isEmpty()) return null
		val firstElement = elements.first()
		val lastElement = elements.last()
		val replacedRange = TextRange.create(firstElement.textRange.startOffset, lastElement.textRange.endOffset)
		val replacedText = replacedRange.substring(firstElement.containingFile.text)
		if(firstElement != lastElement) {
			firstElement.parent.deleteChildRange(firstElement.nextSibling, lastElement)
		}
		val newProperty = CwtElementFactory.createProperty(project, "key", "{\n${replacedText}\n}")
		val replacement = firstElement.replace(newProperty) as CwtProperty
		val newNameElement = replacement.propertyKey
		return newNameElement.textRange
	}
}

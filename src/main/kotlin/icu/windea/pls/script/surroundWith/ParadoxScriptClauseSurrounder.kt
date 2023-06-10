package icu.windea.pls.script.surroundWith

import com.intellij.lang.surroundWith.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

/**
 * 从句的包围器，将选中的表达式（一个或多个属性或者单独的值）用花括号包围。
 * 
 * ```
 * # 应用前：
 * k = v
 * 
 * # 应用后：
 * {
 *     k = v
 * }
 * ```
 */
class ParadoxScriptClauseSurrounder: Surrounder {
	@Suppress("DialogTitleCapitalization")
	override fun getTemplateDescription(): String {
		return PlsBundle.message("script.surroundWith.clause.description")
	}
	
	override fun isApplicable(elements: Array<out PsiElement>): Boolean {
		return true
	}
	
	override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
		if(elements.isEmpty()) return null
		val firstElement = elements.first()
		val lastElement = elements.last()
		val replacedRange = TextRange.create(firstElement.startOffset, lastElement.endOffset)
		val replacedText = replacedRange.substring(firstElement.containingFile.text)
		if(firstElement != lastElement) {
			firstElement.parent.deleteChildRange(firstElement.nextSibling, lastElement)
		}
		var newElement = ParadoxScriptElementFactory.createValue(project, "{\n${replacedText}\n}") as ParadoxScriptBlock
		newElement = firstElement.replace(newElement) as ParadoxScriptBlock
		newElement = CodeStyleManager.getInstance(project).reformat(newElement, true) as ParadoxScriptBlock
		val endOffset = newElement.endOffset
		return TextRange.create(endOffset, endOffset)
	}
}


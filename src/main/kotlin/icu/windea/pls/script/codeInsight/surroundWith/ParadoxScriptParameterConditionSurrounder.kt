package icu.windea.pls.script.codeInsight.surroundWith

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxScriptParameterConditionSurrounder : ParadoxScriptSurrounder() {
    override fun getTemplateDescription(): String {
        return PlsBundle.message("script.surroundWith.parameterCondition.description")
    }

    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        return true
    }

    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange? {
        if (elements.isEmpty()) return null
        val firstElement = elements.first()
        val lastElement = elements.last()
        val replacedRange = TextRange.create(firstElement.startOffset, lastElement.endOffset)
        val replacedText = replacedRange.substring(firstElement.containingFile.text)
        if (firstElement != lastElement) {
            firstElement.parent.deleteChildRange(firstElement.nextSibling, lastElement)
        }
        var newElement = ParadoxScriptElementFactory.createParameterCondition(project, "PARAM", "\n${replacedText}\n")
        newElement = firstElement.replace(newElement) as ParadoxScriptParameterCondition
        newElement = CodeStyleManager.getInstance(project).reformat(newElement, true) as ParadoxScriptParameterCondition
        return newElement.parameterConditionExpression!!.textRange
    }
}

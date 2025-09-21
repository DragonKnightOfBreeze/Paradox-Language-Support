package icu.windea.pls.script.codeInsight.surroundWith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementFactory

class ParadoxScriptBlockSurrounder : Surrounder {
    override fun getTemplateDescription(): String {
        return "{ }"
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
        var newElement = ParadoxScriptElementFactory.createValue(project, "{\n${replacedText}\n}") as ParadoxScriptBlock
        newElement = firstElement.replace(newElement) as ParadoxScriptBlock
        newElement = CodeStyleManager.getInstance(project).reformat(newElement, true) as ParadoxScriptBlock
        val endOffset = newElement.endOffset
        return TextRange.create(endOffset, endOffset)
    }
}


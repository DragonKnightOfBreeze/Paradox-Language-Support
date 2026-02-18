package icu.windea.pls.cwt.codeInsight.surroundWith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtElementFactory

class CwtBlockSurrounder : Surrounder {
    override fun getTemplateDescription() = "{...}"

    override fun isApplicable(elements: Array<out PsiElement>): Boolean {
        return elements.isNotEmpty()
    }

    override fun surroundElements(project: Project, editor: Editor, elements: Array<out PsiElement>): TextRange {
        val firstElement = elements.first()
        val lastElement = elements.last()
        val replacedRange = TextRange.create(firstElement.startOffset, lastElement.endOffset)
        val replacedText = replacedRange.substring(firstElement.containingFile.text)
        if (firstElement != lastElement) {
            firstElement.parent.deleteChildRange(firstElement.nextSibling, lastElement)
        }
        var newElement = CwtElementFactory.createBlockFromText(project, "{\n${replacedText}\n}")
        newElement = firstElement.replace(newElement) as CwtBlock
        newElement = CodeStyleManager.getInstance(project).reformat(newElement, true) as CwtBlock
        return TextRange.from(newElement.endOffset, 0)
    }
}

package icu.windea.pls.script.codeInsight.surroundWith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertySurrounder : Surrounder {
    @Suppress("DialogTitleCapitalization")
    override fun getTemplateDescription(): String {
        return "key = { }"
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
        var newElement = ParadoxScriptElementFactory.createPropertyFromText(project, "key = {\n$replacedText\n}")
        newElement = firstElement.replace(newElement) as ParadoxScriptProperty
        newElement = CodeStyleManager.getInstance(project).reformat(newElement, true) as ParadoxScriptProperty
        return newElement.propertyKey.textRange
    }
}

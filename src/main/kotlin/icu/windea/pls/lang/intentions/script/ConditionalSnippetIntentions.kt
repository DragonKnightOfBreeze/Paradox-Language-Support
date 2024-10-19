package icu.windea.pls.lang.intentions.script

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

private val propertyFormatRegex = "(\\w+)\\s*=\\s*\\$\\1\\|no\\$".toRegex()
private val blockFormatRegex = "\\[\\[(\\w+)]\\s*\\1\\s*=\\s*\\$\\1\\$\\s*]".toRegex()

private val propertyTemplate = { p: String -> "$p = \$$p|no\$" }
private val blockTemplate = { p: String -> "[[$p] $p = \$$p\$ ]" }

class ConditionalSnippetToPropertyFormatIntention : IntentionAction {
    override fun getText() = PlsBundle.message("intention.conditionalSnippetToPropertyFormat")

    override fun getFamilyName() = text

    override fun startInWriteAction() = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return false
        val text = element.text
        return blockFormatRegex.matches(text)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        val text = element.text
        val matchResult = blockFormatRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = propertyTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createDummyFile(project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptProperty>()
            ?: return
        element.replace(newElement)
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) { it.parentOfType<ParadoxScriptParameterCondition>() }
    }
}

class ConditionalSnippetToBlockFormatIntention : IntentionAction {
    override fun getText() = PlsBundle.message("intention.conditionalSnippetToBlockFormat")

    override fun getFamilyName() = text

    override fun startInWriteAction() = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return false
        val text = element.text
        return propertyFormatRegex.matches(text)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        val text = element.text
        val matchResult = propertyFormatRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = blockTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createDummyFile(project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptParameterCondition>()
            ?: return
        element.replace(newElement)
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) { it.parentOfType<ParadoxScriptProperty>() }
    }
}

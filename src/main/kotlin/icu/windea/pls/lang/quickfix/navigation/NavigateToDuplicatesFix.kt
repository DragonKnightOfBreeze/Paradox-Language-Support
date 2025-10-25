package icu.windea.pls.lang.quickfix.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle

class NavigateToDuplicatesFix(
    private val key: String,
    target: PsiElement,
    elements: Collection<PsiElement>
) : NavigateToFix(target, elements, true) {
    override fun getText() = PlsBundle.message("fix.navigateTo.duplicateScriptedVariables.name")

    override fun getPopupTitle(editor: Editor) = PlsBundle.message("fix.navigateTo.duplicateScriptedVariables.popup.title", key)

    override fun getPopupText(editor: Editor, value: PsiElement): String {
        val document = editor.document
        val lineNumber = document.getLineNumber(value.textOffset)
        return PlsBundle.message("fix.navigate.popup.text.atLine", key, lineNumber)
    }
}

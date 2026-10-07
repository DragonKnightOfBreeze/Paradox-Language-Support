package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.ParadoxLanguage

/**
 * 导航到对应的规则。
 */
class GotoRelatedConfigsAction : GotoActionBase() {
    private val handler = GotoRelatedConfigsHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        return file.language is ParadoxLanguage
    }
}

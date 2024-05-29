package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.lang.documentation.actions.*

class ParadoxDocumentationActionProvider: DocumentationActionProvider {
    override fun additionalActions(editor: Editor?, docComment: PsiDocCommentBase?, renderedText: String?): List<AnAction> {
        return listOf(
            Separator(),
            ChangeQuickDocLocalisationLocaleAction()
        )
    }
}
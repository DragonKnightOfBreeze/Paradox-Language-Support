package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand

class EscapeCommandFix(
    element: PsiElement
) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
    override fun getText() = PlsBundle.message("fix.escapeCommand.name")

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val commandElement = startElement.parent?.castOrNull<ParadoxLocalisationCommand>() ?: return
        val startOffset = commandElement.startOffset
        file.fileDocument.insertString(startOffset, "[")
    }
}

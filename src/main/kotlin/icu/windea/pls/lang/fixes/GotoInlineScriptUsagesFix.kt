package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

class GotoInlineScriptUsagesFix(
    element: PsiFile
) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
    override fun getText() = PlsBundle.message("fix.gotoInlineScriptUsages.fix")

    override fun getFamilyName() = text

    override fun isAvailable(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Boolean {
        return ParadoxInlineScriptManager.isInlineScriptFile(file)
    }

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor == null) return
        GotoDeclarationAction.startFindUsages(editor, project, file)
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}

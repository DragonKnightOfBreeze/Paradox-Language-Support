package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.codeInspection.IntentionAndQuickFixAction
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptFile

class GotoInlineScriptUsagesFix : IntentionAndQuickFixAction() {
    override fun getName(): String {
        return PlsBundle.message("goto.usages")
    }

    override fun getFamilyName(): String {
        return name
    }

    override fun applyFix(project: Project, file: PsiFile?, editor: Editor?) {
        if (file == null || editor == null) return
        if (file !is ParadoxScriptFile) return
        val expression = ParadoxInlineScriptManager.getInlineScriptExpression(file)
        if (expression == null) return
        GotoDeclarationAction.startFindUsages(editor, project, file)
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
}

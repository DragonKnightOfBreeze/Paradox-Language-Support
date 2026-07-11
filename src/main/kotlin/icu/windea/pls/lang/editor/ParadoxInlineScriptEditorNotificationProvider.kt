package icu.windea.pls.lang.editor

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.ParadoxScriptFileType
import java.util.function.Function
import javax.swing.JComponent

/**
 * 为内联脚本文件提供提供编辑器通知，以提供明确的提示，以及快速查找用法。
 *
 * 适用于项目内外的任何符合条件的脚本文件。
 */
class ParadoxInlineScriptEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.fileType !is ParadoxScriptFileType) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val message = ChronicleBundle.message("editor.notification.inlineScript.text", inlineScriptExpression)
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(message)
            panel.createActionLabel(ChronicleBundle.message("editor.notification.inlineScript.action.1")) { gotoUsages(file, project, fileEditor) }
            panel
        }
    }

    private fun gotoUsages(file: VirtualFile, project: Project, fileEditor: TextEditor) {
        val psiFile = file.toPsiFile(project) ?: return
        GotoDeclarationAction.startFindUsages(fileEditor.editor, project, psiFile)
    }
}

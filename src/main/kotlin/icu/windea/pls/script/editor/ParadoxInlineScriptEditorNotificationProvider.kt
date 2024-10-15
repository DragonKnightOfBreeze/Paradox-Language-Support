package icu.windea.pls.script.editor

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.*
import java.util.function.Function
import javax.swing.*

/**
 * 为内联脚本文件提供通知，以便快速导航到它们的使用处。
 */
class ParadoxInlineScriptEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.fileType != ParadoxScriptFileType) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val editor = fileEditor.editor
            val psiFile = file.toPsiFile(project) ?: return@f null
            val message = PlsBundle.message("editor.notification.inlineScript", inlineScriptExpression)
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(message)
            panel.createActionLabel(PlsBundle.message("goto.usages")) {
                GotoDeclarationAction.startFindUsages(editor, project, psiFile)
            }
            panel
        }
    }
}

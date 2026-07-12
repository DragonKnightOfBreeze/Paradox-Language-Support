package icu.windea.pls.lang.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.script.ParadoxScriptFileType
import java.util.function.Function
import javax.swing.JComponent

/**
 * 提供编辑器通知，以明确提示当前的脚本文件用于声明全局的封装变量（scripted variables）。
 *
 * 适用于项目内外的任何符合条件的脚本文件。
 */
class ParadoxGlobalScriptedVariablesEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.fileType !is ParadoxScriptFileType) return null

        if (!ParadoxScriptedVariableManager.isGlobalScriptedVariablesFile(file)) return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val message = ChronicleBundle.message("editor.notification.globalScriptedVariables.text")
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(message)
            panel
        }
    }
}

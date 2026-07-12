package icu.windea.pls.lang.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxDescriptorModBasedModMetadata
import icu.windea.pls.model.analysis.ParadoxMetadataJsonBasedModMetadata
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.matchesBy
import java.util.function.Function
import javax.swing.JComponent

/**
 * 如果游戏类型不匹配其使用的模组描述符文件，则为模组文件提供编辑器通知。
 *
 * 仅适用于项目中的文本文件。
 */
class ParadoxGameTypeMismatchedEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val rootFile = rootInfo.rootFile
        if (!rootFile.isValid) return null

        val gameType = rootInfo.gameType
        val metadata = rootInfo.metadata
        val message = when (metadata) {
            is ParadoxDescriptorModBasedModMetadata -> {
                when {
                    gameType matchesBy ParadoxGameTypeConstraint.DescriptorModUsed -> null
                    else -> ChronicleBundle.message("editor.notification.3.text.1", gameType.title)
                }
            }
            is ParadoxMetadataJsonBasedModMetadata -> {
                when {
                    gameType matchesBy ParadoxGameTypeConstraint.MetadataJsonUsed -> null
                    else -> ChronicleBundle.message("editor.notification.3.text.2", gameType.title)
                }
            }
            else -> null
        }
        if (message == null) return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning).text(message)
            panel
        }
    }
}

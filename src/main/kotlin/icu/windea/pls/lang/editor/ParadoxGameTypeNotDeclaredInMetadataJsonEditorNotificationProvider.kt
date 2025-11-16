package icu.windea.pls.lang.editor

import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.jsonMapper
import icu.windea.pls.ep.metadata.ParadoxMetadataJsonBasedModMetadataProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import kotlinx.coroutines.launch
import java.util.function.Function
import javax.swing.JComponent

/**
 * 如果游戏类型未在 `.metadata/metadata.json` 中声明，则为模组文件提供通知，以便快速配置（VIC3 / EU5）。
 * 仅适用于项目中的文本文件。
 */
class ParadoxGameTypeNotDeclaredInMetadataJsonEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.Mod) return null
        val rootFile = rootInfo.rootFile
        if (!rootFile.isValid) return null

        val metadata = rootInfo.metadata
        if (metadata !is ParadoxMetadataJsonBasedModMetadataProvider.Metadata) return null
        if (metadata.inferredGameType != null) return null

        val isInProject = ProjectFileIndex.getInstance(project).isInContent(rootFile)
        if (!isInProject) return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val message = PlsBundle.message("editor.notification.2.text")
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning).text(message)
            val gameTypes = ParadoxGameType.getAllUseMetadataJson()
            for (gameType in gameTypes) {
                panel.createActionLabel(PlsBundle.message("editor.notification.2.action", gameType.gameId, gameType.title)) action@{
                    declareGameType(project, gameType, metadata)
                }
            }
            panel
        }
    }

    private fun declareGameType(project: Project, gameType: ParadoxGameType, metadata: ParadoxMetadataJsonBasedModMetadataProvider.Metadata) {
        val infoFile = metadata.infoFile
        val newInfo = metadata.info.copy(gameId = gameType.gameId)
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            writeCommandAction(project, PlsBundle.message("editor.notification.2.action.command")) {
                jsonMapper.writeValue(infoFile.getOutputStream(this), newInfo)
            }
        }

        // 之后，`ParadoxFileListener` 将会监听到 `.metadata/metadata.json` 的更改，从而进行必要的刷新
    }
}

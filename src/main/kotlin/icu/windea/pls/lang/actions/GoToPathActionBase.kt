package icu.windea.pls.lang.actions

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserPanel
import com.intellij.openapi.fileChooser.FileSystemTree
import com.intellij.openapi.fileChooser.actions.FileChooserAction
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxFileInfo
import java.nio.file.Path

/**
 * 用于在文件选择页面中跳转到一个路径。
 */
@Suppress("UnstableApiUsage")
abstract class GoToPathActionBase : FileChooserAction(), LightEditCompatible {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(panel: FileChooserPanel, e: AnActionEvent) {
        val targetPath = getTargetPath(e)
        e.presentation.isEnabledAndVisible = targetPath != null
    }

    override fun update(fileChooser: FileSystemTree, e: AnActionEvent) {
        val targetPath = getTargetPath(e)
        e.presentation.isEnabledAndVisible = targetPath != null && isUnderRoots(targetPath, fileChooser)
    }

    private fun isUnderRoots(targetPath: Path, fileChooser: FileSystemTree): Boolean {
        runCatchingCancelable {
            val file = targetPath.toVirtualFile() ?: return false
            return fileChooser.isUnderRoots(file)
        }
        return false
    }

    override fun actionPerformed(panel: FileChooserPanel, e: AnActionEvent) {
        runCatchingCancelable {
            val targetPath = getTargetPath(e) ?: return
            panel.load(targetPath)
        }
    }

    override fun actionPerformed(fileChooser: FileSystemTree, e: AnActionEvent) {
        runCatchingCancelable {
            val targetPath = getTargetPath(e) ?: return
            val file = targetPath.toVirtualFile(refreshIfNeed = true) ?: return
            val onDone = if (shouldExpand(fileChooser, e)) Runnable { fileChooser.expand(file, null) } else null
            fileChooser.select(file, onDone)
        }
    }

    protected abstract fun getTargetPath(e: AnActionEvent): Path?

    protected open fun shouldExpand(fileChooser: FileSystemTree, e: AnActionEvent): Boolean = false

    protected fun getFileInfo(e: AnActionEvent): ParadoxFileInfo? {
        val files = PlsFileManager.findFiles(e)
        return files.firstNotNullOfOrNull { it.fileInfo }
    }
}

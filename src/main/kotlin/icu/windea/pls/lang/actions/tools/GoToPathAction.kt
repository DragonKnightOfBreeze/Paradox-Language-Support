package icu.windea.pls.lang.actions.tools

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserPanel
import com.intellij.openapi.fileChooser.FileSystemTree
import com.intellij.openapi.fileChooser.actions.FileChooserAction
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toVirtualFile
import java.nio.file.Path

/**
 * 用于在文件选择页面中跳转到一个路径。
 */
@Suppress("UnstableApiUsage")
abstract class GoToPathAction : FileChooserAction(), LightEditCompatible {
    abstract val targetPath: Path?

    open val expand: Boolean = false

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(panel: FileChooserPanel, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = isVisible(e)
        presentation.isEnabled = presentation.isVisible && targetPath != null
    }

    override fun update(fileChooser: FileSystemTree, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = isVisible(e)
        if (presentation.isEnabled) {
            presentation.isEnabled = presentation.isVisible && runCatchingCancelable r@{
                val targetPath = targetPath ?: return@r false
                val file = targetPath.toVirtualFile() ?: return@r false
                fileChooser.isUnderRoots(file)
            }.getOrElse { false }
        }
    }

    override fun actionPerformed(panel: FileChooserPanel, e: AnActionEvent) {
        runCatchingCancelable {
            val targetPath = targetPath ?: return
            panel.load(targetPath)
        }
    }

    override fun actionPerformed(fileChooser: FileSystemTree, e: AnActionEvent) {
        runCatchingCancelable {
            val targetPath = targetPath ?: return
            val file = targetPath.toVirtualFile(refreshIfNeed = true) ?: return
            fileChooser.select(file, if (expand) Runnable { fileChooser.expand(file, null) } else null)
        }
    }

    protected abstract fun isVisible(e: AnActionEvent): Boolean
}

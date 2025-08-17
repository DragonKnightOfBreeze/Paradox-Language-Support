package icu.windea.pls.lang.actions.tools

import com.intellij.ide.lightEdit.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.fileChooser.actions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import java.nio.file.*

/**
 * 用于在文件选择页面中跳转到一个路径。
 */
@Suppress("UnstableApiUsage")
abstract class GoToPathAction : FileChooserAction(), LightEditCompatible {
    abstract val targetPath: Path?

    open val expand: Boolean = false

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected abstract fun setVisible(e: AnActionEvent): Boolean

    override fun update(panel: FileChooserPanel, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = setVisible(e)
        presentation.isEnabled = presentation.isVisible && targetPath != null
    }

    override fun update(fileChooser: FileSystemTree, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = setVisible(e)
        if (presentation.isEnabled) {
            presentation.isEnabled = presentation.isVisible && runCatchingCancelable {
                val targetPath = targetPath ?: return@runCatchingCancelable false
                val file = VfsUtil.findFile(targetPath, false) ?: return@runCatchingCancelable false
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
            val file = VfsUtil.findFile(targetPath, true) ?: return
            fileChooser.select(file, if (expand) Runnable { fileChooser.expand(file, null) } else null)
        }
    }
}

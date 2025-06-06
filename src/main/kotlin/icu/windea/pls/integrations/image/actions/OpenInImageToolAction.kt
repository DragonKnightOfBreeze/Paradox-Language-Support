package icu.windea.pls.integrations.image.actions

import com.intellij.ide.lightEdit.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.image.*
import icu.windea.pls.integrations.image.providers.*

/**
 * @see PlsImageToolProvider
 */
@Suppress("UnstableApiUsage")
abstract class OpenInImageToolAction : DumbAwareAction(), LightEditCompatible {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val (file, tool) = getFileAndTool(e) ?: return
        presentation.isEnabledAndVisible = tool != null && tool.supports()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val (file, tool) = getFileAndTool(e) ?: return
        if (tool == null || !tool.supports()) return
        tool.open(file)
    }

    protected abstract fun getTool(): PlsImageToolProvider?

    private fun getFileAndTool(e: AnActionEvent): Tuple2<VirtualFile, PlsImageToolProvider?>? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if (!PlsImageManager.isImageFile(file)) return null
        val tool = getTool() ?: return tupleOf(file, null)
        return file to tool
    }
}

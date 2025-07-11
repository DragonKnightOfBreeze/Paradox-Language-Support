package icu.windea.pls.images.actions

import com.intellij.execution.*
import com.intellij.execution.configurations.*
import com.intellij.execution.util.*
import com.intellij.ide.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.io.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.images.*
import org.intellij.images.*
import org.intellij.images.fileTypes.*
import org.intellij.images.options.impl.*
import java.awt.*
import java.io.*

//org.intellij.images.actions.EditExternallyAction

class EditExternallyAction : DumbAwareAction() {
    //这里不能复用IDEA自带的 EditExternallyAction，因为文件类型判断有区别

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val imageFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        var executablePath = PropertiesComponent.getInstance().getValue("Images.ExternalEditorPath", "")
        if (!StringUtil.isEmpty(executablePath)) {
            EnvironmentUtil.getEnvironmentMap().forEach { (varName, varValue) ->
                executablePath = if (SystemInfo.isWindows) StringUtil.replace(executablePath, "%$varName%", varValue, true)
                else StringUtil.replace(executablePath, "\${$varName}", varValue, false)
            }

            executablePath = FileUtil.toSystemDependentName(executablePath)
            val executable = File(executablePath)
            val commandLine = GeneralCommandLine()
            val path = if (executable.exists()) executable.absolutePath else executablePath
            if (SystemInfo.isMac) {
                commandLine.exePath = ExecUtil.openCommandPath
                commandLine.addParameter("-a")
                commandLine.addParameter(path)
            } else {
                commandLine.exePath = path
            }

            val typeManager = ImageFileTypeManager.getInstance()

            if (imageFile.isInLocalFileSystem && typeManager.isImage(imageFile)) {
                commandLine.addParameter(VfsUtilCore.virtualToIoFile(imageFile).absolutePath)
            }
            commandLine.workDirectory = File(executablePath).parentFile

            try {
                commandLine.createProcess()
            } catch (ex: ExecutionException) {
                Messages.showErrorDialog(e.project, ex.localizedMessage, ImagesBundle.message("error.title.launching.external.editor"))
                ImagesConfigurable.show(e.project)
            }
        } else {
            try {
                Desktop.getDesktop().open(imageFile.toNioPath().toFile())
            } catch (ignore: IOException) {
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = file != null && ImageManager.isExtendedImageFileType(file.fileType)
        if (e.isFromContextMenu) {
            e.presentation.isVisible = enabled
        }
        e.presentation.isEnabled = enabled
    }
}

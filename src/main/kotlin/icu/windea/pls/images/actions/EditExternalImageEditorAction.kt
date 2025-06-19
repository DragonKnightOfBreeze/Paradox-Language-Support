package icu.windea.pls.images.actions

import com.intellij.ide.*
import com.intellij.ide.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.ui.dsl.builder.*
import org.intellij.images.*
import javax.swing.*

//org.intellij.images.actions.EditExternalImageEditorAction

class EditExternalImageEditorAction : DumbAwareAction() {
    companion object {
        const val EXT_PATH_KEY = "Images.ExternalEditorPath"

        fun showDialog(project: Project?) {
            EditExternalImageEditorDialog(project).show()
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        showDialog(e.project)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    @Suppress("UnstableApiUsage")
    class EditExternalImageEditorDialog(val project: Project?) : DialogWrapper(project) {

        init {
            title = ImagesBundle.message("edit.external.editor.path.dialog.title")
            setOKButtonText(IdeBundle.message("button.save"))
            init()
        }

        override fun createCenterPanel(): JComponent {
            val fileDescriptor = FileChooserDescriptor(true, SystemInfo.isMac, false, false, false, false)
            fileDescriptor.isShowFileSystemRoots = true
            fileDescriptor.title = ImagesBundle.message("select.external.executable.title")
            fileDescriptor.description = ImagesBundle.message("select.external.executable.message")

            return panel {
                row(ImagesBundle.message("external.editor.executable.path")) {
                    textFieldWithBrowseButton(fileDescriptor, project) { it.path }
                        .bindText(
                            { PropertiesComponent.getInstance().getValue(EXT_PATH_KEY, "") },
                            { PropertiesComponent.getInstance().setValue(EXT_PATH_KEY, it) })
                        .align(AlignX.FILL)
                }
            }
        }
    }
}

package icu.windea.pls.images.actions

import com.intellij.ide.util.DirectoryUtil
import com.intellij.openapi.actionSystem.ActionManager.*
import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.impl.FileChooserUtil
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextComponentAccessors
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.refactoring.RefactoringBundle.*
import com.intellij.ui.EditorTextField
import com.intellij.ui.RecentsManager
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.executeCommand

// com.intellij.refactoring.copy.CopyFilesOrDirectoriesDialog

class ConvertImageFormatDialog(
    private val files: List<PsiFile>,
    private val project: Project,
    private val defaultNewFileName: String?,
    private val targetFormatName: String,
) : DialogWrapper(project, true) {
    private val recentKeys = "Pls.ConvertImageFormat.RECENT_KEYS.$targetFormatName"

    private val defaultTargetDirectory = files.first().parent

    val newFileName: String? get() = newFileNameField?.text?.trim()
    var targetDirectory: PsiDirectory? = defaultTargetDirectory

    private var newFileNameField: EditorTextField? = null
    private var targetDirectoryField: TextFieldWithHistoryWithBrowseButton? = null

    init {
        title = ChronicleBundle.message("convertImageFormat.dialog.title", targetFormatName)
        init()
    }

    // （信息标签）
    // （输入框）文件名
    // （文件路径输入框）目标目录

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                val text = when {
                    files.size == 1 -> {
                        val virtualFile = files.first().virtualFile
                        ChronicleBundle.message("convertImageFormat.dialog.info.0", shortenPath(virtualFile), targetFormatName)
                    }
                    else -> {
                        ChronicleBundle.message("convertImageFormat.dialog.info.1", targetFormatName)
                    }
                }
                label(text).bold()
            }
            if (files.size == 1) {
                row {
                    label(ChronicleBundle.message("convertImageFormat.dialog.newFileName")).widthGroup("left")
                    cell(initNewFileNameField())
                        .align(Align.FILL)
                        .resizableColumn()
                        .focused()
                }
            }
            row {
                label(ChronicleBundle.message("convertImageFormat.dialog.targetDirectory")).widthGroup("left")
                cell(initTargetDirectoryField())
                    .align(Align.FILL)
                    .resizableColumn()
            }
            row {
                val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(getInstance().getAction(ACTION_CODE_COMPLETION))
                comment(message("path.completion.shortcut", shortcutText))
            }
        }.withPreferredWidth(preferredDialogWidth)
    }

    private fun initNewFileNameField(): EditorTextField {
        val newFileName = defaultNewFileName.orEmpty()
        val newFileNameField = EditorTextField()
        newFileNameField.text = newFileName
        newFileNameField.editor.let { editor ->
            if (editor != null) {
                val dotIndex = newFileName.indexOf('.').let { if (it == -1) newFileName.length else it }
                editor.selectionModel.setSelection(0, dotIndex)
                editor.caretModel.moveToOffset(dotIndex)
            } else {
                newFileNameField.selectAll()
            }
        }
        return newFileNameField
    }

    private fun initTargetDirectoryField(): TextFieldWithHistoryWithBrowseButton {
        val targetDirectoryField = TextFieldWithHistoryWithBrowseButton().also { this.targetDirectoryField = it }
        targetDirectoryField.setTextFieldPreferredWidth(preferredPathWidth)
        val recentEntries = RecentsManager.getInstance(project).getRecentEntries(recentKeys)
        val targetDirectoryComponent = targetDirectoryField.childComponent
        val targetPath = defaultTargetDirectory?.virtualFile?.presentableUrl
        if (recentEntries != null) targetDirectoryComponent.history = recentEntries
        targetDirectoryComponent.text = targetPath
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle(ChronicleBundle.message("convertImageFormat.dialog.targetDirectory.title"))
            .withDescription(ChronicleBundle.message("convertImageFormat.dialog.targetDirectory.description"))
        targetDirectoryField.addBrowseFolderListener(project, descriptor, TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT)
        return targetDirectoryField
    }

    private fun shortenPath(file: VirtualFile): String {
        return StringUtil.shortenPathWithEllipsis(file.presentableUrl, preferredPathWidth)
    }

    override fun doOKAction() {
        newFileNameField?.let {
            val newFileName = newFileName
            if (newFileName.isNullOrEmpty()) {
                Messages.showErrorDialog(project, ChronicleBundle.message("convertImageFormat.dialog.newFileName.error"), ChronicleBundle.message("error.title"))
                return
            }
        }
        targetDirectoryField?.let {
            val targetDirectoryName = targetDirectoryField!!.childComponent.text
            if (targetDirectoryName.isEmpty()) {
                Messages.showErrorDialog(project, ChronicleBundle.message("convertImageFormat.dialog.targetDirectory.error"), ChronicleBundle.message("error.title"))
                return
            }

            RecentsManager.getInstance(project).registerRecentEntry(recentKeys, targetDirectoryName)

            executeCommand(project, ChronicleBundle.message("create.directory")) {
                runWriteAction {
                    try {
                        val path = FileUtil.toSystemIndependentName(targetDirectoryName)
                        targetDirectory = DirectoryUtil.mkdirs(PsiManager.getInstance(project), path)
                    } catch (_: IncorrectOperationException) {
                        targetDirectory = null
                    }
                }
            }
            if (targetDirectory == null) {
                Messages.showErrorDialog(project, ChronicleBundle.message("cannot.create.directory"), ChronicleBundle.message("error.title"))
                return
            }
            targetDirectory?.let {
                FileChooserUtil.setLastOpenedFile(project, it.virtualFile.toNioPath())
            }
        }

        super.doOKAction()
    }

    companion object {
        private const val preferredDialogWidth = 600
        private const val preferredPathWidth = 70
    }
}

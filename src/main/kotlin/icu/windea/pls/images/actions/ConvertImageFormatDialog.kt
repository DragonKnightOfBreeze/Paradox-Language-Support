package icu.windea.pls.images.actions

import com.intellij.ide.util.DirectoryUtil
import com.intellij.openapi.actionSystem.ActionManager.getInstance
import com.intellij.openapi.actionSystem.IdeActions.ACTION_CODE_COMPLETION
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.impl.FileChooserUtil
import com.intellij.openapi.keymap.KeymapUtil.getFirstKeyboardShortcutText
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextComponentAccessors
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.refactoring.RefactoringBundle.message
import com.intellij.ui.EditorTextField
import com.intellij.ui.RecentsManager
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.PlsBundle

//com.intellij.refactoring.copy.CopyFilesOrDirectoriesDialog

class ConvertImageFormatDialog(
    private val files: List<PsiFile>,
    private val project: Project,
    private val defaultNewFileName: String?,
    private val targetFormatName: String,
) : DialogWrapper(project, true) {
    private val maxPathLength = 70
    private val recentKeys = "Pls.ConvertImageFormat.RECENT_KEYS.$targetFormatName"

    private val defaultTargetDirectory = files.first().parent

    val newFileName: String? get() = newFileNameField?.text?.trim()
    var targetDirectory: PsiDirectory? = defaultTargetDirectory

    private var newFileNameField: EditorTextField? = null
    private var targetDirectoryField: TextFieldWithHistoryWithBrowseButton? = null

    init {
        title = PlsBundle.message("convertImageFormat.dialog.title", targetFormatName)
        init()
    }

    //（信息标签）
    //（输入框）文件名
    //（文件路径输入框）目标目录

    override fun createCenterPanel() = panel {
        row {
            val text = when {
                files.size == 1 -> {
                    val virtualFile = files.first().virtualFile
                    PlsBundle.message("convertImageFormat.dialog.info.0", shortenPath(virtualFile), targetFormatName)
                }
                else -> {
                    PlsBundle.message("convertImageFormat.dialog.info.1", targetFormatName)
                }
            }
            label(text).bold()
        }
        if (files.size == 1) {
            row {
                label(PlsBundle.message("convertImageFormat.dialog.newFileName")).widthGroup("left")
                cell(initNewFileNameField())
                    .align(Align.FILL)
                    .resizableColumn()
                    .focused()
            }
        }
        row {
            label(PlsBundle.message("convertImageFormat.dialog.targetDirectory")).widthGroup("left")
            cell(initTargetDirectoryField())
                .align(Align.FILL)
                .resizableColumn()
        }
        row {
            val shortcutText = getFirstKeyboardShortcutText(getInstance().getAction(ACTION_CODE_COMPLETION))
            comment(message("path.completion.shortcut", shortcutText))
        }
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
        targetDirectoryField.setTextFieldPreferredWidth(maxPathLength)
        val recentEntries = RecentsManager.getInstance(project).getRecentEntries(recentKeys)
        val targetDirectoryComponent = targetDirectoryField.childComponent
        val targetPath = defaultTargetDirectory?.virtualFile?.presentableUrl
        if (recentEntries != null) targetDirectoryComponent.history = recentEntries
        targetDirectoryComponent.text = targetPath
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle(PlsBundle.message("convertImageFormat.dialog.targetDirectory.title"))
            .withDescription(PlsBundle.message("convertImageFormat.dialog.targetDirectory.description"))
        targetDirectoryField.addBrowseFolderListener(project, descriptor, TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT)
        return targetDirectoryField
    }

    private fun shortenPath(file: VirtualFile): String {
        return StringUtil.shortenPathWithEllipsis(file.presentableUrl, maxPathLength)
    }

    override fun doOKAction() {
        newFileNameField?.let {
            val newFileName = newFileName
            if (newFileName.isNullOrEmpty()) {
                Messages.showErrorDialog(project, PlsBundle.message("convertImageFormat.dialog.newFileName.error"), PlsBundle.message("error.title"))
                return
            }
        }
        targetDirectoryField?.let {
            val targetDirectoryName = targetDirectoryField!!.childComponent.text
            if (targetDirectoryName.isEmpty()) {
                Messages.showErrorDialog(project, PlsBundle.message("convertImageFormat.dialog.targetDirectory.error"), PlsBundle.message("error.title"))
                return
            }

            RecentsManager.getInstance(project).registerRecentEntry(recentKeys, targetDirectoryName)

            executeCommand(project, PlsBundle.message("create.directory"), null) {
                runWriteAction {
                    try {
                        val path = FileUtil.toSystemIndependentName(targetDirectoryName)
                        targetDirectory = DirectoryUtil.mkdirs(PsiManager.getInstance(project), path)
                    } catch (e: IncorrectOperationException) {
                        targetDirectory = null
                    }
                }
            }
            if (targetDirectory == null) {
                Messages.showErrorDialog(project, PlsBundle.message("cannot.create.directory"), PlsBundle.message("error.title"))
                return
            }
            targetDirectory?.let {
                FileChooserUtil.setLastOpenedFile(project, it.virtualFile.toNioPath())
            }
        }

        super.doOKAction()
    }
}

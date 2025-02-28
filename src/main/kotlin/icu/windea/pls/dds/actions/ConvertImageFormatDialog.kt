package icu.windea.pls.dds.actions

import com.intellij.ide.util.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.fileChooser.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.io.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*

//com.intellij.refactoring.copy.CopyFilesOrDirectoriesDialog

@Suppress("CanBeParameter")
class ConvertImageFormatDialog(
    private val sourceFormatName: String,
    private val targetFormatName: String,
    private val files: List<PsiFile>,
    private val project: Project,
    private val defaultNewFileName: String?,
) : DialogWrapper(project, true) {
    private val maxPathLength = 70
    private val recentKeys = "Pls.ConvertImageFormat.RECENT_KEYS.$sourceFormatName.$targetFormatName"

    private val defaultTargetDirectory = files.first().parent

    val newFileName: String? get() = newFileNameField?.text?.trim()
    var targetDirectory: PsiDirectory? = defaultTargetDirectory

    private var newFileNameField: EditorTextField? = null
    private var targetDirectoryField: TextFieldWithHistoryWithBrowseButton? = null

    init {
        title = PlsBundle.message("dds.dialog.convertImageFormat.title", sourceFormatName, targetFormatName)
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
                    PlsBundle.message("dds.dialog.convertImageFormat.info", sourceFormatName, shortenPath(virtualFile))
                }
                else -> {
                    PlsBundle.message("dds.dialog.convertImageFormat.info.1", sourceFormatName)
                }
            }
            label(text).bold()
        }
        if (files.size == 1) {
            row {
                label(PlsBundle.message("dds.dialog.convertImageFormat.newFileName")).widthGroup("left")
                cell(initNewFileNameField())
                    .align(Align.FILL)
                    .resizableColumn()
                    .focused()
            }
        }
        row {
            label(PlsBundle.message("dds.dialog.convertImageFormat.targetDirectory")).widthGroup("left")
            cell(initTargetDirectoryField())
                .align(Align.FILL)
                .resizableColumn()
        }
        row {
            pathCompletionShortcutComment()
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
        targetDirectoryField.addBrowseFolderListener(
            PlsBundle.message("dds.dialog.convertImageFormat.targetDirectory.title"),
            PlsBundle.message("dds.dialog.convertImageFormat.targetDirectory.description"),
            project, descriptor, TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
        )
        return targetDirectoryField
    }

    private fun shortenPath(file: VirtualFile): String {
        return StringUtil.shortenPathWithEllipsis(file.presentableUrl, maxPathLength)
    }

    override fun doOKAction() {
        newFileNameField?.let {
            val newFileName = newFileName
            if (newFileName.isNullOrEmpty()) {
                Messages.showErrorDialog(project, PlsBundle.message("dds.dialog.convertImageFormat.newFileName.error"), PlsBundle.message("error.title"))
                return
            }
        }
        targetDirectoryField?.let {
            val targetDirectoryName = targetDirectoryField!!.childComponent.text
            if (targetDirectoryName.isEmpty()) {
                Messages.showErrorDialog(project, PlsBundle.message("dds.dialog.convertImageFormat.targetDirectory.error"), PlsBundle.message("error.title"))
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

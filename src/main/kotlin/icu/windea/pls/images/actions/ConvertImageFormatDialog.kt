package icu.windea.pls.images.actions

import com.intellij.ide.util.DirectoryUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager.*
import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.impl.FileChooserUtil
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
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
import com.intellij.refactoring.RefactoringBundle
import com.intellij.ui.EditorTextField
import com.intellij.ui.RecentsManager
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.executeCommand

// com.intellij.refactoring.copy.CopyFilesOrDirectoriesDialog

/**
 * 用于配置图片格式转换选项的对话框。
 *
 * 目标格式在下拉框中指定，支持 [supportedTargetFormatNames] 中列出的格式。
 * 单文件模式下，目标文件名会随所选目标格式自动联动扩展名。
 * 上次选择的目标格式会被记忆，作为下次打开对话框时的默认值。
 */
class ConvertImageFormatDialog(
    private val files: List<PsiFile>,
    private val project: Project,
) : DialogWrapper(project, true) {
    private val propertiesComponent = PropertiesComponent.getInstance(project)
    private val initialTargetFormatName = inferTargetFormatName()

    private val defaultTargetDirectory = files.first().parent

    val newFileName: String? get() = newFileNameField?.text?.trim()
    var targetDirectory: PsiDirectory? = defaultTargetDirectory

    /**
     * 当前选定的目标图片格式（大写形式，如 `PNG`）。
     */
    val targetFormatName: String
        get() = targetFormatComboBox?.selectedItem?.toString()?.uppercase() ?: initialTargetFormatName

    private var newFileNameField: EditorTextField? = null
    private var targetDirectoryField: TextFieldWithHistoryWithBrowseButton? = null
    private var targetFormatComboBox: ComboBox<String>? = null

    init {
        title = ChronicleBundle.message("convertImageFormat.dialog.title")
        init()
    }

    // （信息标签）
    // （下拉框）目标格式
    // （输入框）文件名
    // （文件路径输入框）目标目录

    public override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                val text = when {
                    files.size == 1 -> {
                        val virtualFile = files.first().virtualFile
                        ChronicleBundle.message("convertImageFormat.dialog.info.0", shortenPath(virtualFile))
                    }
                    else -> ChronicleBundle.message("convertImageFormat.dialog.info.1")
                }
                label(text).bold()
            }
            row {
                label(ChronicleBundle.message("convertImageFormat.dialog.targetFormat")).widthGroup("left")
                val cell = comboBox(supportedTargetFormatNames)
                    .align(Align.FILL)
                    .resizableColumn()
                targetFormatComboBox = cell.component
                targetFormatComboBox?.selectedItem = initialTargetFormatName
                targetFormatComboBox?.addActionListener { onTargetFormatChanged() }
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
                comment(RefactoringBundle.message("path.completion.shortcut", shortcutText))
            }
        }.withPreferredWidth(preferredDialogWidth)
    }

    private fun initNewFileNameField(): EditorTextField {
        val newFileName = getNewFileName(files.first().name, targetFormatName)
        val newFileNameField = EditorTextField().also { this.newFileNameField = it }
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

    /**
     * 在单文件模式下切换目标格式时，同步替换目标文件名的扩展名，并让选区停留在扩展名之前。
     */
    private fun onTargetFormatChanged() {
        val newFileNameField = newFileNameField ?: return
        val fileName = newFileNameField.text.trim()
        if (fileName.isEmpty()) return
        val newFileName = getNewFileName(fileName, targetFormatName)
        if (newFileName == fileName) return
        newFileNameField.text = newFileName
        val dotIndex = newFileName.indexOf('.').let { if (it == -1) newFileName.length else it }
        newFileNameField.editor?.let { editor ->
            editor.caretModel.moveToOffset(dotIndex)
            editor.selectionModel.setSelection(0, dotIndex)
        }
    }

    /**
     * 推导首次打开对话框时的目标格式：优先使用上次选择，并避免单文件转换时与源文件格式重复。
     */
    private fun inferTargetFormatName(): String {
        val remembered = propertiesComponent.getValue(targetFormatKey)?.toSupportedTargetFormatName() ?: defaultTargetFormatName
        if (files.size != 1) return remembered
        val sourceFormat = files.first().virtualFile?.extension?.toSupportedTargetFormatName() ?: return remembered
        if (remembered != sourceFormat) return remembered
        return if (sourceFormat == "PNG") "DDS" else "PNG"
    }

    private fun String.toSupportedTargetFormatName(): String? {
        val name = trim().removePrefix(".").uppercase()
        return when (name) {
            "JPEG" -> "JPG"
            in supportedTargetFormatNames -> name
            else -> null
        }
    }

    private fun shortenPath(file: VirtualFile): String {
        return StringUtil.shortenPathWithEllipsis(file.presentableUrl, preferredPathWidth)
    }

    /**
     * 仅用于测试或内部联动：以编程方式切换目标格式，行为等同于用户在下拉框中重新选择。
     */
    internal fun selectTargetFormat(targetFormatName: String) {
        targetFormatComboBox?.selectedItem = targetFormatName
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

        propertiesComponent.setValue(targetFormatKey, targetFormatName)
        super.doOKAction()
    }

    companion object {
        private const val defaultTargetFormatName = "PNG"
        private const val targetFormatKey = "Pls.ConvertImageFormat.TARGET_FORMAT"
        private const val recentKeys = "Pls.ConvertImageFormat.RECENT_KEYS"
        private const val preferredDialogWidth = 600
        private const val preferredPathWidth = 70
    }
}

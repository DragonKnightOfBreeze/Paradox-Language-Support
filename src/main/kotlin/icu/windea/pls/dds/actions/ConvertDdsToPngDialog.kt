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
import javax.swing.*

//com.intellij.refactoring.copy.CopyFilesOrDirectoriesDialog

class ConvertDdsToPngDialog(
	private val files: List<PsiFile>,
	private val defaultNewName: String?,
	private val defaultTargetDirectory: PsiDirectory,
	private val project: Project
) : DialogWrapper(project, true) {
	companion object {
		private const val MAX_PATH_LENGTH = 70
		private const val RECENT_KEYS = "ConvertDdsToPng.RECENT_KEYS"
	}
	
	val newName: String? get() = newNameField?.text?.trim()
	var targetDirectory: PsiDirectory? = defaultTargetDirectory
	
	var newNameField: EditorTextField? = null
	var targetDirectoryField: TextFieldWithHistoryWithBrowseButton? = null
	val targetDirectoryComponent get() = targetDirectoryField!!.childComponent
	
	init {
		title = PlsBundle.message("dds.dialog.convertDdsToPng.title")
		init()
	}
	
	override fun getPreferredFocusedComponent(): JComponent? {
		return newNameField
	}
	
	//（信息标签）
	//（输入框）文件名
	//（文件路径输入框）目标目录
	
	override fun createCenterPanel(): JComponent {
		return panel {
			row {
				val text = when {
					files.size == 1 -> {
						val virtualFile = files.first().virtualFile
						PlsBundle.message("dds.dialog.convertDdsToPng.info", shortenPath(virtualFile))
					}
					else -> {
						PlsBundle.message("dds.dialog.convertDdsToPng.info.1")
					}
				}
				label(text).bold()
			}
			if(files.size == 1) {
				row {
					label(PlsBundle.message("dds.dialog.convertDdsToPng.newName")).widthGroup("left")
					cell(initNewNameField())
				}
			}
			row {
				label(PlsBundle.message("dds.dialog.convertDdsToPng.targetDirectory")).widthGroup("left")
				cell(initTargetDirectoryField())
			}
			row{
				pathCompletionShortcutComment()
			}
		}
	}
	
	private fun initNewNameField(): EditorTextField {
		val newName = defaultNewName.orEmpty()
		val newNameField = EditorTextField().also { this.newNameField = it }
		newNameField.text = newName
		newNameField.editor.let { editor ->
			if(editor != null) {
				val dotIndex = newName.indexOf('.').let { if(it == -1) newName.length else it }
				editor.selectionModel.setSelection(0, dotIndex)
				editor.caretModel.moveToOffset(dotIndex)
			} else {
				newNameField.selectAll()
			}
		}
		return newNameField
	}
	
	private fun initTargetDirectoryField(): TextFieldWithHistoryWithBrowseButton {
		val targetDirectoryField = TextFieldWithHistoryWithBrowseButton().also { this.targetDirectoryField = it }
		targetDirectoryField.setTextFieldPreferredWidth(MAX_PATH_LENGTH)
		val recentEntries = RecentsManager.getInstance(project).getRecentEntries(RECENT_KEYS)
		val targetDirectoryComponent = targetDirectoryComponent
		val targetPath = defaultTargetDirectory.virtualFile.presentableUrl
		targetDirectoryComponent.text = targetPath
		if(recentEntries != null) targetDirectoryComponent.history = recentEntries
		val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
		targetDirectoryField.addBrowseFolderListener(
			PlsBundle.message("dds.dialog.convertDdsToPng.targetDirectory.title"),
			PlsBundle.message("dds.dialog.convertDdsToPng.targetDirectory.description"),
			project, descriptor, TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
		)
		return targetDirectoryField
	}
	
	private fun shortenPath(file: VirtualFile): String {
		return StringUtil.shortenPathWithEllipsis(file.presentableUrl, MAX_PATH_LENGTH)
	}
	
	override fun doOKAction() {
		newNameField?.let {
			val newName = newName
			if(newName.isNullOrEmpty()) {
				Messages.showErrorDialog(project, PlsBundle.message("dds.dialog.convertDdsToPng.newName.error"), PlsBundle.message("error.title"))
				return
			}
		}
		targetDirectoryField?.let {
			val targetDirectoryName = targetDirectoryComponent.text
			if(targetDirectoryName.isEmpty()) {
				Messages.showErrorDialog(project, PlsBundle.message("dds.dialog.convertDdsToPng.targetDirectory.error"), PlsBundle.message("error.title"))
				return
			}
			
			CommandProcessor.getInstance().executeCommand(project, {
				ApplicationManager.getApplication().runWriteAction {
					try {
						val path = FileUtil.toSystemIndependentName(targetDirectoryName)
						targetDirectory = DirectoryUtil.mkdirs(PsiManager.getInstance(project), path)
					} catch(ignored: IncorrectOperationException) {
						targetDirectory = null
					}
				}
			}, PlsBundle.message("create.directory"), null)
			if(targetDirectory == null){
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

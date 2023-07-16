package icu.windea.pls.script.refactoring

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*

class IntroduceGlobalScriptedVariableDialog(
	private val project: Project,
	private val scriptedVariablesFile: VirtualFile,
	variableName: String,
	variableValue: String? = null,
	private val suggestedVariableNames: List<String>? = null
) : DialogWrapper(project, true) {
	companion object {
		private const val MAX_PATH_LENGTH = 70
		private const val RECENT_KEYS = "Pls.IntroduceGlobalScriptedVariable.RECENT_KEYS"
	}
	
	private val setVariableValue = variableValue != null
	
	private val propertyGraph = PropertyGraph()
	private val variableNameProperty = propertyGraph.property(variableName)
	private val variableValueProperty = propertyGraph.property(variableValue.orEmpty())
	
	var variableName by variableNameProperty
	var variableValue by variableValueProperty
	var file = scriptedVariablesFile
	var filePath = file.path
	
	private val fileField = TextFieldWithHistoryWithBrowseButton()
	
	init {
		title = PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.title")
		init()
	}
	
	//（输入框）输入变量名
	//（输入框）输入变量值
	//（文件选择框）选择目标文件
	
	override fun createCenterPanel() = panel {
		row {
			//输入变量名
			label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName")).widthGroup("left")
			textField()
				.bindText(variableNameProperty)
				.align(Align.FILL)
				.resizableColumn()
				.focused()
				.validationOnApply { validateScriptedVariableName() }
		}
		if(setVariableValue) {
			row {
				//输入变量值
				label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableValue")).widthGroup("left")
				textField()
					.bindText(variableValueProperty)
					.align(Align.FILL)
					.resizableColumn()
					.focused()
					.validationOnApply { validateScriptedVariableValue() }
			}
		}
		row {
			//选择目标文件 - 仅允许用户选择同一游戏或模组根目录下的common/scripted_variables目录下的文件
			label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile")).widthGroup("left")
			val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ParadoxScriptFileType)
				.withRoots(scriptedVariablesFile)
				.withTreeRootVisible(true)
			val fileField = fileField.apply {
				setTextFieldPreferredWidth(MAX_PATH_LENGTH)
				val recentEntries = RecentsManager.getInstance(project).getRecentEntries(RECENT_KEYS)
				if(recentEntries != null) childComponent.history = recentEntries
				childComponent.text = recentEntries?.firstOrNull() ?: filePath
				addBrowseFolderListener(
					PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.browseDialogTitle"),
					null,
					project,
					descriptor,
					TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT
				)
			}
			cell(fileField)
				.align(Align.FILL)
				.resizableColumn()
				.validationRequestor { validator -> fileField.childComponent.textEditor.whenTextChanged { validator() } }
				.validationOnApply { validateScriptedVariableFilePath() }
		}
		row {
			pathCompletionShortcutComment()
		}
	}.apply {
		withPreferredWidth(width * 2) //2倍宽度 - 基于调试结果
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableName(): ValidationInfo? {
		if(variableName.isEmpty()) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName.invalid.0"))
		} else if(!PlsConstants.Patterns.scriptedVariableNameRegex.matches(variableName)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName.invalid.1"))
		}
		return null
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableValue(): ValidationInfo? {
		if(variableValue.isEmpty()) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableValue.invalid.0"))
		} else if(!ParadoxType.resolve(variableValue).canBeScriptedVariableValue()) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableValue.invalid.1"))
		}
		return null
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableFilePath(): ValidationInfo? {
		filePath = fileField.text
		if(filePath.isEmpty()) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.0"))
		} else if(!filePath.endsWith(".txt", true)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.3"))
		}
		val selectedFile = VfsUtil.findFile(filePath.toPath(), false)
			?.takeIf { it.exists() }
			?: return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.1"))
		val pathToEntry = selectedFile.fileInfo?.pathToEntry?.path
		if(pathToEntry == null || !"common/scripted_variables".matchesPath(pathToEntry, acceptSelf = false)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.2"))
		}
		file = selectedFile
		return null
	}
	
	override fun doOKAction() {
		RecentsManager.getInstance(project).registerRecentEntry(RECENT_KEYS, filePath)
		
		super.doOKAction()
	}
}

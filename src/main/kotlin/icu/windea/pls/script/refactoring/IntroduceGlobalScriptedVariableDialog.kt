package icu.windea.pls.script.refactoring

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import javax.swing.*

class IntroduceGlobalScriptedVariableDialog(
	private val project: Project,
	private val scriptedVariablesFile: VirtualFile,
) : DialogWrapper(project, true) {
	var file = scriptedVariablesFile.contentFile
	
	private val propertyGraph = PropertyGraph()
	private val variableNameProperty = propertyGraph.property(defaultScriptedVariableName)
	private val filePathProperty = propertyGraph.property(file.path)
	
	var variableName by variableNameProperty
	var filePath by filePathProperty
	
	init {
		title = PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.title")
		init()
	}
	
	//（输入框）输入变量名
	//（文件选择框）选择目标文件
	
	override fun createCenterPanel(): JComponent {
		return panel {
			val dialog = this@IntroduceGlobalScriptedVariableDialog
			row {
				//输入变量名
				textField()
					.bindText(dialog.variableNameProperty)
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
					.focused()
					.validationOnApply { validateScriptedVariableName() }
					.label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName"))
			}
			row {
				//选择目标文件 - 仅允许用户选择同一游戏或模组根目录下的common/scripted_variables目录下的文件
				val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ParadoxScriptFileType)
					.withRoots(scriptedVariablesFile)
					.withTreeRootVisible(true)
				textFieldWithBrowseButton(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.browseDialogTitle"), project, descriptor) { it.path }
					.bindText(dialog.filePathProperty)
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
					.validationOnApply { validateScriptedVariableFilePath() }
					.label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile"))
			}
		}.apply {
			withPreferredWidth(width * 2) //2倍宽度 - 基于调试结果
		}
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableName(): ValidationInfo? {
		if(!PlsPatterns.scriptedVariableNameRegex.matches(variableName)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName.invalid", variableName))
		}
		return null
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableFilePath(): ValidationInfo? {
		val file = VfsUtil.findFile(filePath.toPath(), false)
			?.takeIf { it.exists() }
			?: return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.0"))
		this@IntroduceGlobalScriptedVariableDialog.file = file
		val path = file.fileInfo?.path?.path
		if(path == null || !"common/scripted_variables".matchesPath(path, acceptSelf = false)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.1"))
		}
		return null
	}
	
	override fun doOKAction() {
		scriptedVariablesFile.contentFile = file
		super.doOKAction()
	}
}
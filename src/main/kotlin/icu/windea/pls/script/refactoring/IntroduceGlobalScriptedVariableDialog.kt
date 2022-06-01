package icu.windea.pls.script.refactoring

import com.intellij.openapi.fileChooser.*
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
	var variableName = defaultScriptedVariableName
	var file = scriptedVariablesFile.contentFile
	var filePath = file.path
	
	private lateinit var panel: DialogPanel
	
	init {
		title = PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.title")
		init()
	}
	
	override fun createCenterPanel(): JComponent {
		return panel {
			val dialog = this@IntroduceGlobalScriptedVariableDialog
			row {
				//输入变量名
				textField()
					.bindText(dialog::variableName)
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
				textFieldWithBrowseButton(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.chooseDestination"), project, descriptor) { it.also { file = it }.path }
					.bindText(dialog::filePath)
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
					.validationOnApply { validateScriptedVariableFilePath() }
					.label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile"), LabelPosition.TOP)
			}
		}.apply {
			withPreferredWidth(width * 2) //2倍宽度 - 基于调试结果
			panel = this
		}
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableName(): ValidationInfo? {
		if(!PlsPatterns.scriptedVariableNameRegex.matches(variableName)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName.invalid", variableName))
		}
		return null
	}
	
	private fun ValidationInfoBuilder.validateScriptedVariableFilePath(): ValidationInfo? {
		//val virtualFile = VfsUtil.findFile(Path.of(filePath), false)
		val path = file.fileInfo?.path?.path
		if(path == null || !"common/scripted_variables".matchesPath(path, acceptSelf = false)) {
			return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid"))
		}
		return null
	}
	
	override fun doOKAction() {
		panel.apply()
		scriptedVariablesFile.contentFile = file
		super.doOKAction()
	}
}
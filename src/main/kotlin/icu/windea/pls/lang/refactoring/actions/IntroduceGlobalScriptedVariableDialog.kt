package icu.windea.pls.lang.refactoring.actions

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextComponentAccessors
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.RecentsManager
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.pathCompletionShortcutComment
import icu.windea.pls.core.toPath
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.constants.PlsPatternConstants
import icu.windea.pls.script.ParadoxScriptFileType

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
        if (setVariableValue) {
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
            //选择目标文件 - 仅允许用户选择同一入口目录下的common/scripted_variables目录下的文件
            label(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile")).widthGroup("left")
            val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(ParadoxScriptFileType)
                .withTitle(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.browseDialogTitle"))
                .withRoots(scriptedVariablesFile)
                .withTreeRootVisible(true)
            val fileField = fileField.apply {
                setTextFieldPreferredWidth(MAX_PATH_LENGTH)
                val recentEntries = RecentsManager.getInstance(project).getRecentEntries(RECENT_KEYS)
                if (recentEntries != null) childComponent.history = recentEntries
                childComponent.text = recentEntries?.firstOrNull() ?: filePath
                addBrowseFolderListener(project, descriptor, TextComponentAccessors.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT)
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
        if (variableName.isEmpty()) {
            return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName.invalid.0"))
        } else if (!PlsPatternConstants.scriptedVariableName.matches(variableName)) {
            return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableName.invalid.1"))
        }
        return null
    }

    private fun ValidationInfoBuilder.validateScriptedVariableValue(): ValidationInfo? {
        if (variableValue.isEmpty()) {
            return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableValue.invalid.0"))
        } else if (!ParadoxTypeResolver.resolve(variableValue).canBeScriptedVariableValue()) {
            return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.variableValue.invalid.1"))
        }
        return null
    }

    private fun ValidationInfoBuilder.validateScriptedVariableFilePath(): ValidationInfo? {
        filePath = fileField.text
        if (filePath.isEmpty()) {
            return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.0"))
        } else if (!filePath.endsWith(".txt", true)) {
            return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.3"))
        }
        val selectedFile = VfsUtil.findFile(filePath.toPath(), false)
            ?.takeIf { it.exists() }
            ?: return error(PlsBundle.message("script.dialog.introduceGlobalScriptedVariable.extractToFile.invalid.1"))
        val path = selectedFile.fileInfo?.path?.path
        if (path == null || !"common/scripted_variables".matchesPath(path, acceptSelf = false)) {
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

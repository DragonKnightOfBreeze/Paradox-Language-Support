package icu.windea.pls.lang.actions.cwt

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.trim
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.RecentsManager
import com.intellij.ui.components.textFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.util.generators.CwtConfigGenerator
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.ui.bindText
import icu.windea.pls.core.ui.textFieldWithHistoryWithBrowseButton
import icu.windea.pls.model.ParadoxGameType
import javax.swing.JComponent
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

class GenerateConfigDialog(
    private val project: Project,
    private val generator: CwtConfigGenerator,
) : DialogWrapper(project) {
    private val defaultGameType = PlsFacade.getSettings().defaultGameType
    private val gameTypes = ParadoxGameType.getAll()

    private val gameTypeProperty = AtomicProperty(defaultGameType)
    private val inputPathProperty = AtomicProperty("")
    private val outputPathProperty = AtomicProperty("")

    val gameType by gameTypeProperty
    val inputPath by inputPathProperty
    val outputPath by outputPathProperty

    init {
        title = PlsBundle.message("config.generation.dialog.title", generator.getName())
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        // gameType
        row(PlsBundle.message("config.generation.dialog.field.gameType")) {
            comboBox(gameTypes, textListCellRenderer { it?.title })
                .bindItem(gameTypeProperty)
        }

        // inputPath
        row(PlsBundle.message("config.generation.dialog.field.inputPath")) {
            val descriptor0 = when {
                generator.fromScripts -> FileChooserDescriptorFactory.singleDir()
                else -> FileChooserDescriptorFactory.singleFile().withExtensionFilter("log")
            }
            val descriptor = descriptor0
                .withTitle(PlsBundle.message("config.generation.dialog.field.inputPath.title"))
            cell(textFieldWithHistoryWithBrowseButton(project, descriptor, { getInputPathHistories() }))
                // .applyToComponent { setTextFieldPreferredWidth(MAX_PATH_LENGTH) }
                .align(Align.FILL)
                .bindText(inputPathProperty.trim())
                .validationOnApply { validateInputPath() }
        }
        row {
            when {
                generator.fromScripts -> comment(PlsBundle.message("config.generation.dialog.field.inputPath.comment"))
                else -> comment(PlsBundle.message("config.generation.dialog.field.inputPath.commentFromScripts"))
            }
        }

        // outputPath
        row(PlsBundle.message("config.generation.dialog.field.outputPath")) {
            val descriptor = FileChooserDescriptorFactory.singleFile()
                .withExtensionFilter("cwt")
                .withTitle(PlsBundle.message("config.generation.dialog.field.outputPath.title"))
            textFieldWithHistoryWithBrowseButton(descriptor, project, { getOutputPathHistories() })
                // .applyToComponent { setTextFieldPreferredWidth(MAX_PATH_LENGTH) }
                .align(Align.FILL)
                .bindText(outputPathProperty.trim())
                .validationOnApply { validateOutputPath() }
        }
        row {
            comment(PlsBundle.message("config.generation.dialog.field.outputPath.comment"))
        }
        row {
            comment(PlsBundle.message("config.generation.dialog.field.outputPath.commentForName", generator.getGeneratedFileName()))
        }
    }

    private fun getInputPathHistories() = RecentsManager.getInstance(project).getRecentEntries(RECENT_KEYS_INPUT_PATH).orEmpty()

    private fun getOutputPathHistories() = RecentsManager.getInstance(project).getRecentEntries(RECENT_KEYS_OUTPUT_PATH).orEmpty()

    private fun ValidationInfoBuilder.validateInputPath(): ValidationInfo? {
        val v = inputPath
        if (v.isEmpty()) return error(PlsBundle.message("config.generation.dialog.validation.path.empty"))
        val p = v.toPathOrNull()
        when {
            p == null -> return error(PlsBundle.message("config.generation.dialog.validation.path.invalid"))
            generator.fromScripts && !p.isDirectory() -> return error(PlsBundle.message("config.generation.dialog.validation.path.invalid"))
            !generator.fromScripts && !p.extension.equals("log", true) -> return error(PlsBundle.message("config.generation.dialog.validation.path.invalid"))
            !generator.fromScripts && !p.isAbsolute -> return error(PlsBundle.message("config.generation.dialog.validation.path.absolute"))
            p.notExists() -> return error(PlsBundle.message("config.generation.dialog.validation.path.notExists"))
        }
        return null
    }

    private fun ValidationInfoBuilder.validateOutputPath(): ValidationInfo? {
        val v = outputPath
        if (v.isEmpty()) return error(PlsBundle.message("config.generation.dialog.validation.path.empty"))
        val p = v.toPathOrNull()
        when {
            p == null -> return error(PlsBundle.message("config.generation.dialog.validation.path.invalid"))
            !p.isRegularFile() || !p.extension.equals("cwt", true) -> return error(PlsBundle.message("config.generation.dialog.validation.path.invalid"))
            !p.isAbsolute -> return error(PlsBundle.message("config.generation.dialog.validation.path.absolute"))
            p.notExists() -> return error(PlsBundle.message("config.generation.dialog.validation.path.notExists"))
        }
        return null
    }

    companion object {
        private const val MAX_PATH_LENGTH = 70
        private const val RECENT_KEYS_INPUT_PATH = "GenerateConfigDialog.RECENT_KEYS.inputPath"
        private const val RECENT_KEYS_OUTPUT_PATH = "GenerateConfigDialog.RECENT_KEYS.outputPath"
    }
}

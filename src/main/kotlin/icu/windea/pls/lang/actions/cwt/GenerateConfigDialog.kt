package icu.windea.pls.lang.actions.cwt

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.trim
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.RecentsManager
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.util.generators.CwtConfigGenerator
import icu.windea.pls.config.util.generators.CwtConfigGeneratorUtil
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.ui.bindText
import icu.windea.pls.core.ui.textFieldWithHistoryWithBrowseButton
import icu.windea.pls.model.ParadoxGameType
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

class GenerateConfigDialog(
    private val project: Project,
    private val generator: CwtConfigGenerator,
) : DialogWrapper(project) {
    private val propertiesComponent = PropertiesComponent.getInstance(project)
    private val inputPathKey = "Pls.ConfigGeneration.inputPath.${generator.getName()}"
    private val outputPathKey = "Pls.ConfigGeneration.outputPath.${generator.getName()}"

    private val recentsManager = RecentsManager.getInstance(project)
    private val inputPathRecentKeys = "Pls.ConfigGeneration.inputPath.RECENT_KEYS.${generator.getName()}"
    private val outputPathRecentKeys = "Pls.ConfigGeneration.outputPath.RECENT_KEYS.${generator.getName()}"

    private val gameTypeProperty = AtomicProperty(PlsFacade.getSettings().defaultGameType)
    private val inputPathProperty = AtomicProperty(getDefaultInputPath())
    private val outputPathProperty = AtomicProperty(getDefaultOutputPath())

    val gameType by gameTypeProperty
    val inputPath by inputPathProperty
    val outputPath by outputPathProperty

    init {
        title = PlsBundle.message("config.generation.dialog.title", generator.getName())
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            // gameType
            row(PlsBundle.message("config.generation.dialog.field.gameType")) {
                comboBox(ParadoxGameType.getAll(), textListCellRenderer { it?.title })
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
                textFieldWithHistoryWithBrowseButton(descriptor, project, { getInputPathHistories() })
                    .align(Align.FILL)
                    .bindText(inputPathProperty.trim())
                    .validationOnApply { validateInputPath() }
            }
            row {
                when {
                    generator.fromScripts -> comment(PlsBundle.message("config.generation.dialog.field.inputPath.commentFromScripts"))
                    else -> comment(PlsBundle.message("config.generation.dialog.field.inputPath.comment"))
                }
            }
            row {
                comment(PlsBundle.message("config.generation.dialog.field.inputPath.commentForName", generator.getDefaultInputName()))
            }

            // outputPath
            row(PlsBundle.message("config.generation.dialog.field.outputPath")) {
                val descriptor = FileChooserDescriptorFactory.singleFile()
                    .withExtensionFilter("cwt")
                    .withTitle(PlsBundle.message("config.generation.dialog.field.outputPath.title"))
                textFieldWithHistoryWithBrowseButton(descriptor, project, { getOutputPathHistories() })
                    .align(Align.FILL)
                    .bindText(outputPathProperty.trim())
                    .validationOnApply { validateOutputPath() }
            }
            row {
                comment(PlsBundle.message("config.generation.dialog.field.outputPath.comment"))
            }
            row {
                comment(PlsBundle.message("config.generation.dialog.field.outputPath.commentForName", generator.getDefaultOutputName()))
            }

            // quickSelect
            row {
                link(PlsBundle.message("config.generation.dialog.quickSelect.inputPath")) f@{
                    val quickInputPath = CwtConfigGeneratorUtil.getQuickInputPath(gameType, generator)?.toString()?.orNull() ?: return@f
                    inputPathProperty.set(quickInputPath)
                }
                when {
                    generator.fromScripts -> contextHelp(PlsBundle.message("config.generation.dialog.quickSelect.inputPath.tipFromScripts"))
                    else -> contextHelp(PlsBundle.message("config.generation.dialog.quickSelect.inputPath.tip"))
                }
            }
        }.withPreferredWidth(PREFERRED_DIALOG_WIDTH)
    }

    override fun doOKAction() {
        saveDefaults()
        registerHistories()
        super.doOKAction()
    }

    override fun getDimensionServiceKey() = "Pls.GenerateConfigDialog" // 持久化对话框的位置

    private fun getDefaultInputPath() = propertiesComponent.getValue(inputPathKey, "")

    private fun getDefaultOutputPath() = propertiesComponent.getValue(outputPathKey, "")

    private fun saveDefaults() {
        propertiesComponent.setValue(inputPathKey, inputPath)
        propertiesComponent.setValue(outputPathKey, outputPath)
    }

    private fun getInputPathHistories() = recentsManager.getRecentEntries(inputPathRecentKeys).orEmpty()

    private fun getOutputPathHistories() = recentsManager.getRecentEntries(outputPathRecentKeys).orEmpty()

    private fun registerHistories() {
        if (inputPath.isNotEmpty()) recentsManager.registerRecentEntry(inputPathRecentKeys, inputPath)
        if (outputPath.isNotEmpty()) recentsManager.registerRecentEntry(outputPathRecentKeys, outputPath)
    }

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
        private const val PREFERRED_DIALOG_WIDTH = 600
    }
}

package icu.windea.pls.lang.ui.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.jbTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.application
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.integrations.lints.LintToolConstants
import icu.windea.pls.integrations.settings.PlsIntegrationsSettingsManager
import icu.windea.pls.lang.actions.PlsDataKeys
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.lang.listeners.ParadoxModGameTypeListener
import icu.windea.pls.lang.listeners.ParadoxModSettingsListener
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo

@Suppress("UnstableApiUsage")
class ParadoxModSettingsDialog(
    val project: Project,
    val rootInfo: ParadoxRootInfo.Mod,
    val settings: ParadoxModSettingsState
) : DialogWrapper(project, true) {
    private val callbackLock = CallbackLock()

    private val inferredGameTypeInfo = rootInfo.gameTypeInfo

    private val finalGameType = settings.finalGameType
    private val defaultGameDirectory = PlsSettings.getInstance().state.defaultGameDirectories[finalGameType.id]
    private val defaultGameVersion = ParadoxGameManager.getGameVersionFromGameDirectory(defaultGameDirectory)

    private val graph = PropertyGraph()
    private val gameTypeProperty = graph.property(finalGameType)
    private val gameVersionProperty = graph.property(settings.gameVersion.orEmpty())
    private val gameDirectoryProperty = graph.property(settings.gameDirectory.orEmpty())

    private val modDependencies = settings.copyModDependencies()

    init {
        handleModSettings()
        title = PlsBundle.message("mod.settings")
        init()
    }

    private fun handleModSettings() {
        gameVersionProperty.dependsOn(gameDirectoryProperty) { ParadoxGameManager.getGameVersionFromGameDirectory(gameDirectoryProperty.get()).orEmpty() }

        // 如果需要，加上缺失的模组自身的模组依赖配置
        if (modDependencies.find { it.modDirectory == settings.modDirectory } == null) {
            val newSettings = ParadoxModDependencySettingsState()
            newSettings.modDirectory = settings.modDirectory
            modDependencies.add(newSettings)
        }
    }

    override fun createCenterPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            row {
                // name
                label(PlsBundle.message("mod.settings.name")).widthGroup("left")
                textField()
                    .text(settings.name.orEmpty())
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                // version
                label(PlsBundle.message("mod.settings.version")).widthGroup("left")
                textField()
                    .text(settings.version.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                // supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                // modDirectory
                label(PlsBundle.message("mod.settings.modDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .text(settings.modDirectory.orEmpty())
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                // gameType
                label(PlsBundle.message("mod.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.getAllSpecific(), textListCellRenderer { it?.title })
                    .bindItem(gameTypeProperty)
                    .columns(COLUMNS_SHORT)
                    .enabled(inferredGameTypeInfo == null) // disabled if game type can be inferred
                // gameVersion
                label(PlsBundle.message("mod.settings.gameVersion")).widthGroup("right")
                textField()
                    .applyToComponent { defaultGameVersion?.orNull()?.let { emptyText.text = it } }
                    .bindText(gameVersionProperty)
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
            }
            row {
                // gameDirectory
                label(PlsBundle.message("mod.settings.gameDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .applyToComponent { defaultGameDirectory?.orNull()?.let { jbTextField.emptyText.text = it } }
                    .bindText(gameDirectoryProperty)
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .validationOnApply { validateGameDirectory(this) }
            }
            row {
                link(PlsBundle.message("gameDirectory.quickSelect")) { quickSelectGameDirectory() }
            }
            if (inferredGameTypeInfo != null) {
                row {
                    comment(PlsBundle.message("mod.settings.comment.1", inferredGameTypeInfo.gameType.title, inferredGameTypeInfo.lazyMessage.get()))
                }
            }

            // options
            collapsibleGroup(PlsBundle.message("mod.options"), false) {
                // disableTiger
                row {
                    checkBox(PlsBundle.message("mod.options.disableTiger")).bindSelected(settings.options::disableTiger)
                        .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock) }
                    browserLink(PlsBundle.message("link.website"), LintToolConstants.Tiger.url)
                }
                row {
                    comment(PlsBundle.message("mod.options.comment.1"))
                }
            }

            // modDependencies
            collapsibleGroup(PlsBundle.message("mod.dependencies"), false) {
                row {
                    cell(ParadoxModDependenciesTable.createPanel(project, settings, modDependencies)).align(Align.FILL)
                }.resizableRow() // 占据额外的垂直空间
                row {
                    comment(PlsBundle.message("mod.dependencies.comment.1"))
                }
            }.resizableRow() // 占据额外的垂直空间
        }
    }

    private fun validateGameDirectory(builder: ValidationInfoBuilder): ValidationInfo? {
        return ParadoxGameManager.validateGameDirectory(builder, gameTypeProperty.get(), gameDirectoryProperty.get())
    }

    private fun quickSelectGameDirectory() {
        val quickGameDirectory = ParadoxGameManager.getQuickGameDirectory(gameTypeProperty.get())?.orNull() ?: return
        gameDirectoryProperty.set(quickGameDirectory)
    }

    override fun doOKAction() {
        super.doOKAction()

        settings.gameType = gameTypeProperty.get()
        settings.gameDirectory = gameDirectoryProperty.get()
        settings.modDependencies = modDependencies
        PlsProfilesSettings.getInstance().state.updateSettings()
        val messageBus = application.messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(settings)
        if (finalGameType != settings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(settings)
        }
    }

    override fun getDimensionServiceKey() = "Pls.ParadoxModSettingsDialog"
}

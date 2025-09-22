package icu.windea.pls.integrations.settings

import com.intellij.ide.DataManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ai.settings.PlsAiSettingsConfigurable
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.integrations.PlsIntegrationConstants

@Suppress("UnstableApiUsage")
class PlsIntegrationsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.integrations")), SearchableConfigurable {
    override fun getId() = "pls.integrations"

    override fun getHelpTopic() = "icu.windea.pls.integrations.settings"

    private val groupNameImage = "pls.integrations.image"
    private val groupNameLint = "pls.integrations.lint"
    private val callbackLock = CallbackLock()

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        val settings = PlsFacade.getIntegrationsSettings()
        return panel {
            //image tools
            group(PlsBundle.message("settings.integrations.image")) {
                val imageSettings = settings.image
                lateinit var cbMagick: JBCheckBox

                row {
                    comment(PlsBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.image.comment1"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.texconv")).bindSelected(imageSettings::enableTexconv)
                        .comment(PlsBundle.message("settings.integrations.image.from.texconv.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Texconv.url)
                }
                //enableMagick
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.magick")).bindSelected(imageSettings::enableMagick)
                        .comment(PlsBundle.message("settings.integrations.image.from.magick.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                        .applyToComponent { cbMagick = this }
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Magick.url)
                }
                //magickPath
                row {
                    label(PlsBundle.message("settings.integrations.image.magickPath")).widthGroup(groupNameImage)
                    val descriptor = FileChooserDescriptorFactory.singleFile()
                        .withTitle(PlsBundle.message("settings.integrations.image.magickPath.title"))
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(imageSettings::magickPath.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsIntegrationConstants.Magick.pathTip()) }
                        .align(Align.FILL)
                        .validationOnInput { PlsIntegrationsSettingsManager.validateMagickPath(this, it) }
                }.enabledIf(cbMagick.selected)
            }
            //translation tools
            group(PlsBundle.message("settings.integrations.translation")) {
                row {
                    comment(PlsBundle.message("settings.integrations.translation.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.translation.from.tp")).selected(true).enabled(false)
                        .comment(PlsBundle.message("settings.integrations.translation.from.tp.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.TranslationPlugin.url)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.translation.from.ai")).selected(true).enabled(false)
                    link(PlsBundle.message("settings.integrations.translation.from.ai.link")) {
                        DataManager.getInstance().dataContextFromFocusAsync.then {
                            //直接转到AI设置页面
                            Settings.KEY.getData(it)?.let { settings ->
                                settings.find(PlsAiSettingsConfigurable::class.java)?.let { configurable ->
                                    settings.select(configurable)
                                }
                            }

                            //这会嵌套打开AI设置页面
                            //ShowSettingsUtil.getInstance().showSettingsDialog(null, PlsAiSettingsConfigurable::class.java)
                        }
                    }
                }
            }
            //linting tools
            group(PlsBundle.message("settings.integrations.lint")) {
                val lintSettings = settings.lint
                lateinit var cbTiger: JBCheckBox

                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment1"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                //enableTiger
                row {
                    checkBox(PlsBundle.message("settings.integrations.lint.tiger")).bindSelected(lintSettings::enableTiger)
                        .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock) }
                        .applyToComponent { cbTiger = this }
                    browserLink(PlsBundle.message("settings.integrations.website"), PlsIntegrationConstants.Tiger.url)
                }

                val map = PlsIntegrationsSettingsManager.getTigerSettingsMap(settings)
                map.forEach { (gameType, tuple) ->
                    val (name, pathProp, confPathProp) = tuple

                    row {
                        label(PlsBundle.message("settings.integrations.lint.tigerPath", name)).widthGroup(groupNameLint)
                        val descriptor = FileChooserDescriptorFactory.singleFile()
                            .withTitle(PlsBundle.message("settings.integrations.lint.tigerPath.title", name))
                        textFieldWithBrowseButton(descriptor, null)
                            .bindText(pathProp.toNonNullableProperty(""))
                            .applyToComponent { setEmptyState(PlsIntegrationConstants.Tiger.pathTip(gameType)) }
                            .align(Align.FILL)
                            .validationOnInput { PlsIntegrationsSettingsManager.validateTigerPath(this, it, gameType) }
                            .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
                    }.enabledIf(cbTiger.selected)
                    row {
                        label(PlsBundle.message("settings.integrations.lint.tigerConfPath", name)).widthGroup(groupNameLint)
                        val descriptor = FileChooserDescriptorFactory.singleFile()
                            //.withExtensionFilter("conf") //这里不预先按扩展名过滤
                            .withTitle(PlsBundle.message("settings.integrations.lint.tigerConfPath.title", name))
                        textFieldWithBrowseButton(descriptor, null)
                            .bindText(confPathProp.toNonNullableProperty(""))
                            .applyToComponent { setEmptyState(PlsIntegrationConstants.Tiger.confPathTip(gameType)) }
                            .validationOnInput { PlsIntegrationsSettingsManager.validateTigerConfPath(this, it, gameType) }
                            .align(Align.FILL)
                            .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
                    }.enabledIf(cbTiger.selected)
                }

                // tigerHighlighting
                row {
                    label(PlsBundle.message("settings.integrations.lint.tigerHighlight"))
                    contextHelp(PlsBundle.message("settings.integrations.lint.tigerHighlight.tip"))

                    link(PlsBundle.message("configure")) {
                        // Tiger highlight mapping - open dialog - save settings and refresh files after dialog closed with ok
                        val dialog = PlsTigerHighlightDialog()
                        if (dialog.showAndGet()) PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock)
                    }
                }.enabledIf(cbTiger.selected)
            }
        }
    }
}

package icu.windea.pls.integrations.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
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
        val settings = PlsIntegrationsSettings.getInstance().state
        return panel {
            // image tools
            group(PlsBundle.message("settings.integrations.image")) {
                val imageSettings = settings.image

                row {
                    comment(PlsBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.image.comment1"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                // enableTexconv
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.texconv"))
                        .comment(PlsBundle.message("settings.integrations.image.from.texconv.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                        .bindSelected(imageSettings::enableTexconv)
                    browserLink(PlsBundle.message("link.website"), PlsIntegrationConstants.Texconv.url)
                }
                // enableMagick
                row {
                    checkBox(PlsBundle.message("settings.integrations.image.from.magick"))
                        .comment(PlsBundle.message("settings.integrations.image.from.magick.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                        .bindSelected(imageSettings::enableMagick)
                    browserLink(PlsBundle.message("link.website"), PlsIntegrationConstants.Magick.url)
                }
                // magickPath
                row {
                    label(PlsBundle.message("settings.integrations.image.magickPath")).widthGroup(groupNameImage)
                    val descriptor = FileChooserDescriptorFactory.singleFile()
                        .withTitle(PlsBundle.message("settings.integrations.image.magickPath.title"))
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(imageSettings::magickPath.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsIntegrationConstants.Magick.pathTip()) }
                        .align(Align.FILL)
                        .validationOnInput { PlsIntegrationsSettingsManager.validateMagickPath(this, it) }
                }
            }
            // translation tools
            group(PlsBundle.message("settings.integrations.translation")) {
                row {
                    comment(PlsBundle.message("settings.integrations.translation.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.translation.from.tp")).selected(true).enabled(false)
                        .comment(PlsBundle.message("settings.integrations.translation.from.tp.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    browserLink(PlsBundle.message("link.website"), PlsIntegrationConstants.TranslationPlugin.url)
                    link(PlsBundle.message("link.install")) { PlsIntegrationsSettingsManager.installTranslationPlugin() }
                }
                row {
                    checkBox(PlsBundle.message("settings.integrations.translation.from.ai")).selected(true).enabled(false)
                    link(PlsBundle.message("link.configureInSettingsPage")) { PlsIntegrationsSettingsManager.openAiSettingsPage() }
                }
            }
            // linting tools
            group(PlsBundle.message("settings.integrations.lint")) {
                val lintSettings = settings.lint

                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                row {
                    comment(PlsBundle.message("settings.integrations.lint.comment1"), MAX_LINE_LENGTH_WORD_WRAP)
                }
                // enableTiger
                row {
                    checkBox(PlsBundle.message("settings.integrations.lint.tiger"))
                        .bindSelected(lintSettings::enableTiger)
                        .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock) }
                    browserLink(PlsBundle.message("link.website"), PlsIntegrationConstants.Tiger.url)
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
                    }
                    row {
                        label(PlsBundle.message("settings.integrations.lint.tigerConfPath", name)).widthGroup(groupNameLint)
                        val descriptor = FileChooserDescriptorFactory.singleFile()
                            // .withExtensionFilter("conf") // 这里不预先按扩展名过滤
                            .withTitle(PlsBundle.message("settings.integrations.lint.tigerConfPath.title", name))
                        textFieldWithBrowseButton(descriptor, null)
                            .bindText(confPathProp.toNonNullableProperty(""))
                            .applyToComponent { setEmptyState(PlsIntegrationConstants.Tiger.confPathTip(gameType)) }
                            .validationOnInput { PlsIntegrationsSettingsManager.validateTigerConfPath(this, it, gameType) }
                            .align(Align.FILL)
                            .onApply { PlsIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
                    }
                }

                // tigerHighlighting
                row {
                    label(PlsBundle.message("settings.integrations.lint.tigerHighlight"))
                    link(PlsBundle.message("link.configure")) {
                        // Tiger highlight mapping - open dialog - save settings and refresh files after dialog closed with ok
                        val dialog = PlsTigerHighlightDialog()
                        if (dialog.showAndGet()) PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock)
                    }
                    contextHelp(PlsBundle.message("settings.integrations.lint.tigerHighlight.tip"))
                }
            }
        }
    }
}

package icu.windea.pls.integrations.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.ide.help.ChronicleHelpTopics
import icu.windea.pls.integrations.ChronicleIntegrationsBundle
import icu.windea.pls.integrations.images.ImageToolConstants
import icu.windea.pls.integrations.lints.LintToolConstants
import icu.windea.pls.integrations.translation.TranslationToolConstants

@Suppress("UnstableApiUsage")
class ChronicleIntegrationsSettingsConfigurable : BoundConfigurable(ChronicleIntegrationsBundle.message("settings.integrations")), SearchableConfigurable {
    private val callbackLock = CallbackLock()

    override fun getId() = "pls.integrations"

    override fun getHelpTopic() = ChronicleHelpTopics.integrationsSettings

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            // image tools
            group(ChronicleIntegrationsBundle.message("settings.integrations.image")) { configureGroupForImage() }
            // translation tools
            group(ChronicleIntegrationsBundle.message("settings.integrations.translation")) { configureGroupForTranslation() }
            // linting tools
            group(ChronicleIntegrationsBundle.message("settings.integrations.lint")) { configureGroupForLint() }
        }
    }

    private fun Panel.configureGroupForImage() {
        val groupName = "pls.integrations.image"
        val settings = ChronicleIntegrationsSettings.getInstance().state.image

        row {
            comment(ChronicleIntegrationsBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        // enableTexconv
        row {
            checkBox(ChronicleIntegrationsBundle.message("settings.integrations.image.from.texconv"))
                .comment(ChronicleIntegrationsBundle.message("settings.integrations.image.from.texconv.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableTexconv)
            browserLink(ChronicleBundle.message("link.website"), ImageToolConstants.Texconv.url)
        }
        // enableMagick
        row {
            checkBox(ChronicleIntegrationsBundle.message("settings.integrations.image.from.magick"))
                .comment(ChronicleIntegrationsBundle.message("settings.integrations.image.from.magick.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableMagick)
            browserLink(ChronicleBundle.message("link.website"), ImageToolConstants.Magick.url)
        }
        // magickPath
        row {
            label(ChronicleIntegrationsBundle.message("settings.integrations.image.magickPath")).widthGroup(groupName)
            val descriptor = FileChooserDescriptorFactory.singleFile()
                .withTitle(ChronicleIntegrationsBundle.message("settings.integrations.image.magickPath.title"))
            textFieldWithBrowseButton(descriptor, null)
                .bindText(settings::magickPath.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(ImageToolConstants.Magick.pathTip()) }
                .align(Align.FILL)
                .validationOnInput { ChronicleIntegrationsSettingsManager.validateMagickPath(this, it) }
        }
    }

    private fun Panel.configureGroupForTranslation() {
        row {
            comment(ChronicleIntegrationsBundle.message("settings.integrations.translation.comment"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        row {
            checkBox(ChronicleIntegrationsBundle.message("settings.integrations.translation.from.tp")).selected(true).enabled(false)
                .comment(ChronicleIntegrationsBundle.message("settings.integrations.translation.from.tp.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            browserLink(ChronicleBundle.message("link.website"), TranslationToolConstants.TranslationPlugin.url)
            link(ChronicleBundle.message("link.install")) { ChronicleIntegrationsSettingsManager.installTranslationPlugin() }
        }
        row {
            checkBox(ChronicleIntegrationsBundle.message("settings.integrations.translation.from.ai")).selected(true).enabled(false)
                .comment(ChronicleIntegrationsBundle.message("settings.integrations.translation.from.ai.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            link(ChronicleBundle.message("link.configureInSettingsPage")) { ChronicleIntegrationsSettingsManager.openAiSettingsPage() }
        }
    }

    private fun Panel.configureGroupForLint() {
        val groupName = "pls.integrations.lint"
        val settings = ChronicleIntegrationsSettings.getInstance().state.lint

        row {
            comment(ChronicleIntegrationsBundle.message("settings.integrations.lint.comment"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        // enableTiger
        row {
            checkBox(ChronicleIntegrationsBundle.message("settings.integrations.lint.tiger"))
                .comment(ChronicleIntegrationsBundle.message("settings.integrations.lint.tiger.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableTiger)
                .onApply { ChronicleIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock) }
            browserLink(ChronicleBundle.message("link.website"), LintToolConstants.Tiger.url)
        }

        val map = ChronicleIntegrationsSettingsManager.getTigerSettingsMap(ChronicleIntegrationsSettings.getInstance().state)
        map.forEach { (gameType, tuple) ->
            val (name, pathProp, confPathProp) = tuple

            row {
                label(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerPath", name)).widthGroup(groupName)
                val descriptor = FileChooserDescriptorFactory.singleFile()
                    .withTitle(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerPath.title", name))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(pathProp.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(LintToolConstants.Tiger.pathTip(gameType)) }
                    .align(Align.FILL)
                    .validationOnInput { ChronicleIntegrationsSettingsManager.validateTigerPath(this, it, gameType) }
                    .onApply { ChronicleIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
            }
            row {
                label(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerConfPath", name)).widthGroup(groupName)
                val descriptor = FileChooserDescriptorFactory.singleFile()
                    // .withExtensionFilter("conf") // 这里不预先按扩展名过滤
                    .withTitle(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerConfPath.title", name))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(confPathProp.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(LintToolConstants.Tiger.confPathTip(gameType)) }
                    .validationOnInput { ChronicleIntegrationsSettingsManager.validateTigerConfPath(this, it, gameType) }
                    .align(Align.FILL)
                    .onApply { ChronicleIntegrationsSettingsManager.onTigerSettingsChanged(gameType, callbackLock) }
            }
        }

        // tigerHighlighting
        row {
            label(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerHighlight"))
            link(ChronicleBundle.message("link.configure")) {
                // Tiger highlight mapping - open dialog - save settings and refresh files after dialog closed with ok
                val dialog = TigerHighlightDialog()
                if (dialog.showAndGet()) ChronicleIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock)
            }
            contextHelp(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerHighlight.tip"))
        }
    }
}

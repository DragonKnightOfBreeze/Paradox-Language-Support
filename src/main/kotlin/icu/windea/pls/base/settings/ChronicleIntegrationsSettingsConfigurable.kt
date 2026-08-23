package icu.windea.pls.base.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.base.ChronicleBaseBundle
import icu.windea.pls.base.analysis.ChronicleAnalysisManager
import icu.windea.pls.base.help.ChronicleHelpTopics
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.options.OptionsService
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.integrations.images.ImageToolConstants
import icu.windea.pls.integrations.images.ImageToolProvider
import icu.windea.pls.integrations.images.providers.MagickToolProvider
import icu.windea.pls.integrations.lints.LintToolConstants
import icu.windea.pls.integrations.lints.LintToolProvider
import icu.windea.pls.integrations.lints.TigerLintToolService
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.integrations.translation.TranslationToolConstants
import icu.windea.pls.model.ParadoxGameType

@Suppress("UnstableApiUsage")
class ChronicleIntegrationsSettingsConfigurable : BoundConfigurable(ChronicleBaseBundle.message("settings.integrations")), SearchableConfigurable {
    private val callbackLock = CallbackLock()

    override fun getId() = "chronicle.integrations"

    override fun getHelpTopic() = ChronicleHelpTopics.integrationsSettings

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            // image tools
            group(ChronicleBaseBundle.message("settings.integrations.image")) { configureGroupForImage() }
            // translation tools
            group(ChronicleBaseBundle.message("settings.integrations.translation")) { configureGroupForTranslation() }
            // linting tools
            group(ChronicleBaseBundle.message("settings.integrations.lint")) { configureGroupForLint() }
        }
    }

    private fun Panel.configureGroupForImage() {
        val groupName = "integrations.image"
        val settings = ChronicleIntegrationsSettings.getInstance().state.image

        row {
            comment(ChronicleBaseBundle.message("settings.integrations.image.comment"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        row {
            comment(ChronicleBaseBundle.message("settings.integrations.image.comment.impl"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        // enableTexconv
        row {
            checkBox(ChronicleBaseBundle.message("settings.integrations.image.from.texconv"))
                .comment(ChronicleBaseBundle.message("settings.integrations.image.from.texconv.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableTexconv)
            browserLink(ChronicleBundle.message("link.website"), ImageToolConstants.Texconv.url)
        }
        // enableMagick
        row {
            checkBox(ChronicleBaseBundle.message("settings.integrations.image.from.magick"))
                .comment(ChronicleBaseBundle.message("settings.integrations.image.from.magick.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableMagick)
            browserLink(ChronicleBundle.message("link.website"), ImageToolConstants.Magick.url)
        }
        // magickPath
        row {
            label(ChronicleBaseBundle.message("settings.integrations.image.magickPath")).widthGroup(groupName)
            val descriptor = FileChooserDescriptorFactory.singleFile()
                .withTitle(ChronicleBaseBundle.message("settings.integrations.image.magickPath.title"))
            textFieldWithBrowseButton(descriptor, null)
                .bindText(settings::magickPath.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(ImageToolConstants.Magick.pathTip()) }
                .align(Align.FILL)
                .validationOnInput { Manager.validateMagickPath(this, it) }
        }
    }

    private fun Panel.configureGroupForTranslation() {
        row {
            comment(ChronicleBaseBundle.message("settings.integrations.translation.comment"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        row {
            checkBox(ChronicleBaseBundle.message("settings.integrations.translation.from.tp")).selected(true).enabled(false)
                .comment(ChronicleBaseBundle.message("settings.integrations.translation.from.tp.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            browserLink(ChronicleBundle.message("link.website"), TranslationToolConstants.TranslationPlugin.url)
            link(ChronicleBundle.message("link.install")) { Manager.installTranslationPlugin() }
        }
        row {
            checkBox(ChronicleBaseBundle.message("settings.integrations.translation.from.ai")).selected(true).enabled(false)
                .comment(ChronicleBaseBundle.message("settings.integrations.translation.from.ai.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            link(ChronicleBundle.message("link.configureInSettingsPage")) { Manager.openAiSettingsPage() }
        }
    }

    private fun Panel.configureGroupForLint() {
        val groupName = "integrations.lint"
        val settings = ChronicleIntegrationsSettings.getInstance().state.lint

        row {
            comment(ChronicleBaseBundle.message("settings.integrations.lint.comment"), MAX_LINE_LENGTH_WORD_WRAP)
        }
        // enableTiger
        row {
            checkBox(ChronicleBaseBundle.message("settings.integrations.lint.tiger"))
                .comment(ChronicleBaseBundle.message("settings.integrations.lint.tiger.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableTiger)
                .onApply { Manager.onTigerSettingsChanged(callbackLock) }
            browserLink(ChronicleBundle.message("link.website"), LintToolConstants.Tiger.url)
        }

        val map = Manager.getTigerSettingsMap(ChronicleIntegrationsSettings.getInstance().state)
        map.forEach { (gameType, tuple) ->
            val (name, pathProp, confPathProp) = tuple

            row {
                label(ChronicleBaseBundle.message("settings.integrations.lint.tigerPath", name)).widthGroup(groupName)
                val descriptor = FileChooserDescriptorFactory.singleFile()
                    .withTitle(ChronicleBaseBundle.message("settings.integrations.lint.tigerPath.title", name))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(pathProp.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(LintToolConstants.Tiger.pathTip(gameType)) }
                    .align(Align.FILL)
                    .validationOnInput { Manager.validateTigerPath(this, it, gameType) }
                    .onApply { Manager.onTigerSettingsChanged(gameType, callbackLock) }
            }
            row {
                label(ChronicleBaseBundle.message("settings.integrations.lint.tigerConfPath", name)).widthGroup(groupName)
                val descriptor = FileChooserDescriptorFactory.singleFile()
                    // .withExtensionFilter("conf") // 这里不预先按扩展名过滤
                    .withTitle(ChronicleBaseBundle.message("settings.integrations.lint.tigerConfPath.title", name))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(confPathProp.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(LintToolConstants.Tiger.confPathTip(gameType)) }
                    .validationOnInput { Manager.validateTigerConfPath(this, it, gameType) }
                    .align(Align.FILL)
                    .onApply { Manager.onTigerSettingsChanged(gameType, callbackLock) }
            }
        }

        // tigerHighlighting
        with(Factory) { configureForHighlight(callbackLock) }
    }

    object Factory {
        fun Panel.configureForHighlight(callbackLock: CallbackLock) {
            row {
                label(ChronicleBaseBundle.message("settings.integrations.lint.tigerHighlight"))
                contextHelp(ChronicleBaseBundle.message("settings.integrations.lint.tigerHighlight.tip"))

                link(ChronicleBundle.message("link.configure")) {
                    // Tiger highlight mapping - open dialog - save settings and refresh files after dialog closed with ok
                    val dialog = TigerHighlightDialog()
                    if (dialog.showAndGet()) Manager.onTigerSettingsChanged(callbackLock)
                }
            }
        }
    }

    object Manager {
        // Image Tools

        fun validateMagickPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton): ValidationInfo? {
            val path = button.text.trim()
            if (path.isEmpty()) return null
            val tool = ImageToolProvider.EP_NAME.findExtension(MagickToolProvider::class.java) ?: return null
            if (tool.isValidExePath(path)) return null
            return builder.warning(ChronicleBaseBundle.message("settings.integrations.validation.invalidPath"))
        }

        // Translation Tools

        fun installTranslationPlugin() {
            // NOTE 这里需要先切换到插件市场分页，并设置查询关键字
            OptionsService.selectPlugin("Translation", openMarketplaceTab = true)
        }

        fun openAiSettingsPage() {
            OptionsService.select<ChronicleAiSettingsConfigurable>()
        }

        // Lint Tools

        fun getTigerSettingsMap(settings: ChronicleIntegrationsSettings.State) = buildMap {
            put(ParadoxGameType.Ck3, tupleOf("ck3-tiger", settings.lint::ck3TigerPath, settings.lint::ck3TigerConfPath))
            put(ParadoxGameType.Ir, tupleOf("imperator-tiger", settings.lint::irTigerPath, settings.lint::irTigerConfPath))
            put(ParadoxGameType.Vic3, tupleOf("vic3-tiger", settings.lint::vic3TigerPath, settings.lint::vic3TigerConfPath))
        }

        fun validateTigerPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton, gameType: ParadoxGameType): ValidationInfo? {
            val path = button.text.trim()
            if (path.isEmpty()) return null
            val tool = LintToolProvider.EP_NAME.extensionList.findIsInstance<TigerLintToolProvider> { it.isAvailable(gameType) } ?: return null
            if (tool.isValidExePath(path)) return null
            return builder.warning(ChronicleBaseBundle.message("settings.integrations.lint.tigerPath.invalid"))
        }

        @Suppress("UNUSED_PARAMETER")
        fun validateTigerConfPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton, gameType: ParadoxGameType): ValidationInfo? {
            val path = button.text.trim()
            if (path.endsWith(".conf", true)) return null
            return builder.warning(ChronicleBaseBundle.message("settings.integrations.lint.tigerConfPath.invalid"))
        }

        fun onTigerSettingsChanged(callbackLock: CallbackLock) {
            if (!callbackLock.check("onTigerSettingsChanged")) return
            ChronicleAnalysisManager.refreshInlayHints()
        }

        fun onTigerSettingsChanged(gameType: ParadoxGameType, callbackLock: CallbackLock) {
            onTigerSettingsChanged(callbackLock)
            if (!callbackLock.check("onTigerSettingsChanged.${gameType.id}")) return
            TigerLintToolService.getInstance().getModificationTracker(gameType).incModificationCount()
        }
    }
}

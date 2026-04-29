package icu.windea.pls.integrations.settings

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.ai.settings.PlsAiSettingsConfigurable
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.ide.util.PlsDaemonManager
import icu.windea.pls.ide.util.PlsOptionsManager
import icu.windea.pls.integrations.PlsIntegrationsBundle
import icu.windea.pls.integrations.images.ImageToolProvider
import icu.windea.pls.integrations.images.providers.MagickToolProvider
import icu.windea.pls.integrations.lints.LintToolProvider
import icu.windea.pls.integrations.lints.TigerLintToolService
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.model.ParadoxGameType

@Suppress("unused")
object PlsIntegrationsSettingsManager {
    // Image Tools

    fun validateMagickPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton): ValidationInfo? {
        val path = button.text.trim()
        if (path.isEmpty()) return null
        val tool = ImageToolProvider.EP_NAME.findExtension(MagickToolProvider::class.java) ?: return null
        if (tool.isValidExePath(path)) return null
        return builder.warning(PlsIntegrationsBundle.message("settings.integrations.invalidPath"))
    }

    // Translation Tools

    fun installTranslationPlugin() {
        // NOTE 这里需要先切换到插件市场分页，并设置查询关键字
        PlsOptionsManager.selectPlugin("Translation", openMarketplaceTab = true)
    }

    fun openAiSettingsPage() {
        PlsOptionsManager.select<PlsAiSettingsConfigurable>()
    }

    // Lint Tools

    fun getTigerSettingsMap(settings: PlsIntegrationsSettings.State) = buildMap {
        put(ParadoxGameType.Ck3, tupleOf("ck3-tiger", settings.lint::ck3TigerPath, settings.lint::ck3TigerConfPath))
        put(ParadoxGameType.Ir, tupleOf("imperator-tiger", settings.lint::irTigerPath, settings.lint::irTigerConfPath))
        put(ParadoxGameType.Vic3, tupleOf("vic3-tiger", settings.lint::vic3TigerPath, settings.lint::vic3TigerConfPath))
    }

    fun validateTigerPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton, gameType: ParadoxGameType): ValidationInfo? {
        val path = button.text.trim()
        if (path.isEmpty()) return null
        val tool = LintToolProvider.EP_NAME.extensionList.findIsInstance<TigerLintToolProvider> { it.isAvailable(gameType) } ?: return null
        if (tool.isValidExePath(path)) return null
        return builder.warning(PlsIntegrationsBundle.message("settings.integrations.lint.tigerPath.invalid"))
    }

    fun validateTigerConfPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton, gameType: ParadoxGameType): ValidationInfo? {
        val path = button.text.trim()
        if (path.endsWith(".conf", true)) return null
        return builder.warning(PlsIntegrationsBundle.message("settings.integrations.lint.tigerConfPath.invalid"))
    }

    fun onTigerSettingsChanged(callbackLock: CallbackLock) {
        if (!callbackLock.check("onTigerSettingsChanged")) return

        val files = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsDaemonManager.refreshFiles(files, refreshInlayHints = false)
    }

    fun onTigerSettingsChanged(gameType: ParadoxGameType, callbackLock: CallbackLock) {
        onTigerSettingsChanged(callbackLock)

        if (!callbackLock.check("onTigerSettingsChanged.${gameType.id}")) return

        TigerLintToolService.getInstance().getModificationTracker(gameType).incModificationCount()
    }
}

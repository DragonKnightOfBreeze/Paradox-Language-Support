@file:Suppress("unused")

package icu.windea.pls.integrations.settings

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.integrations.images.tools.PlsImageToolProvider
import icu.windea.pls.integrations.images.tools.PlsMagickToolProvider
import icu.windea.pls.integrations.lints.PlsTigerLintManager
import icu.windea.pls.integrations.lints.tools.PlsLintToolProvider
import icu.windea.pls.integrations.lints.tools.PlsTigerLintToolProvider
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType

object PlsIntegrationsSettingsManager {
    // Image Tools

    fun validateMagickPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton): ValidationInfo? {
        val path = button.text.trim()
        if (path.isEmpty()) return null
        val tool = PlsImageToolProvider.EP_NAME.findExtension(PlsMagickToolProvider::class.java) ?: return null
        if (tool.validatePath(path)) return null
        return builder.warning(PlsBundle.message("settings.integrations.invalidPath"))
    }

    // Lint Tools

    fun getTigerSettingsMap(settings: PlsIntegrationsSettingsState) = buildMap {
        put(ParadoxGameType.Ck3, tupleOf("ck3-tiger", settings.lint::ck3TigerPath, settings.lint::ck3TigerConfPath))
        put(ParadoxGameType.Ir, tupleOf("imperator-tiger", settings.lint::irTigerPath, settings.lint::irTigerConfPath))
        put(ParadoxGameType.Vic3, tupleOf("vic3-tiger", settings.lint::vic3TigerPath, settings.lint::vic3TigerConfPath))
    }

    fun validateTigerPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton, gameType: ParadoxGameType): ValidationInfo? {
        val path = button.text.trim()
        if (path.isEmpty()) return null
        val tool = PlsLintToolProvider.EP_NAME.extensionList.findIsInstance<PlsTigerLintToolProvider> { it.isAvailable(gameType) } ?: return null
        if (tool.validatePath(path)) return null
        return builder.warning(PlsBundle.message("settings.integrations.lint.tigerPath.invalid"))
    }

    fun validateTigerConfPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton, gameType: ParadoxGameType): ValidationInfo? {
        val path = button.text.trim()
        if (path.endsWith(".conf", true)) return null
        return builder.warning(PlsBundle.message("settings.integrations.lint.tigerConfPath.invalid"))
    }

    fun onTigerSettingsChanged(callbackLock: CallbackLock) {
        if (!callbackLock.check("onTigerSettingsChanged")) return

        val files = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.refreshFiles(files, refreshInlayHints = false)
    }

    fun onTigerSettingsChanged(gameType: ParadoxGameType, callbackLock: CallbackLock) {
        onTigerSettingsChanged(callbackLock)

        if (!callbackLock.check("onTigerSettingsChanged.${gameType.id}")) return

        PlsTigerLintManager.modificationTrackers.getValue(gameType).incModificationCount()
    }
}

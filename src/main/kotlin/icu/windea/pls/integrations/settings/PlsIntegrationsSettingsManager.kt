@file:Suppress("unused")

package icu.windea.pls.integrations.settings

import com.intellij.openapi.ui.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.integrations.images.tools.*
import icu.windea.pls.integrations.lints.*
import icu.windea.pls.integrations.lints.tools.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

object PlsIntegrationsSettingsManager {
    fun validateMagickPath(builder: ValidationInfoBuilder, button: TextFieldWithBrowseButton): ValidationInfo? {
        val path = button.text.trim()
        if (path.isEmpty()) return null
        val tool = PlsImageToolProvider.EP_NAME.findExtension(PlsMagickToolProvider::class.java) ?: return null
        if (tool.validatePath(path)) return null
        return builder.warning(PlsBundle.message("settings.integrations.invalidPath"))
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

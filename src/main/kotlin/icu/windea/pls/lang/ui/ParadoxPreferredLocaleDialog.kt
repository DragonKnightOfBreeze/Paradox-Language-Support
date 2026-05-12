package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.settings.PlsSettingsManager

class ParadoxPreferredLocaleDialog : DialogWrapper(null, false) {
    private val callbackLock = CallbackLock()

    init {
        title = PlsBundle.message("ui.selectPreferredLocale.title")
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            val settings = PlsSettings.getInstance().state
            var preferredLocale = settings.preferredLocale
            localeComboBox(withAuto = true)
                .bindItem(settings::preferredLocale.toNullableProperty())
                .onApply {
                    val oldPreferredLocale = preferredLocale.orEmpty()
                    val newPreferredLocale = settings.preferredLocale.orEmpty()
                    if (oldPreferredLocale == newPreferredLocale) return@onApply
                    preferredLocale = newPreferredLocale
                    PlsSettingsManager.onPreferredLocaleChanged(callbackLock, oldPreferredLocale, newPreferredLocale)
                }
        }
    }
}

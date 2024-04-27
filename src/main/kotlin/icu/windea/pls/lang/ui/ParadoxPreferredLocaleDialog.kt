package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*

class ParadoxPreferredLocaleDialog : DialogWrapper(null, false) {
    init {
        title = PlsBundle.message("ui.dialog.selectPreferred.title")
        init()
    }
    
    override fun createCenterPanel() = panel {
        row {
            val settings = getSettings()
            val oldPreferredLocale = settings.preferredLocale
            localeComboBox(settings)
                .bindItem(settings::preferredLocale.toNullableProperty())
                .onApply {
                    if(oldPreferredLocale != settings.preferredLocale) {
                        val openedFiles = ParadoxCoreHandler.findOpenedFiles()
                        ParadoxCoreHandler.reparseFiles(openedFiles, reparse = false)
                    }
                }
        }
    }
}
package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.*

class ParadoxPreferredLocaleDialog : DialogWrapper(null, false) {
    init {
        title = PlsBundle.message("ui.dialog.selectPreferred.title")
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            val settings = PlsFacade.getSettings()
            localeComboBox(withAuto = true).bindItem(settings::preferredLocale.toNullableProperty())
                .onApply {
                    refreshOnlyForOpenedFiles()
                }
        }
    }

    private fun refreshOnlyForOpenedFiles() {
        val files = PlsManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsManager.reparseAndRefreshFiles(files, reparse = false)
    }
}

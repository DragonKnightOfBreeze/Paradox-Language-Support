package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.util.PlsCoreManager

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
                    refreshForOpenedFiles()
                }
        }
    }

    private fun refreshForOpenedFiles() {
        val files = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.refreshFiles(files)
    }
}

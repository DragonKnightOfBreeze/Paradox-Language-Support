package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.ide.analysis.PlsAnalysisManager
import icu.windea.pls.lang.settings.PlsSettings

class ParadoxPreferredLocaleDialog : DialogWrapper(null, false) {
    init {
        title = PlsBundle.message("ui.selectPreferredLocale.title")
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            val settings = PlsSettings.getInstance().state
            localeComboBox(withAuto = true).bindItem(settings::preferredLocale.toNullableProperty())
                .onApply {
                    refreshForOpenedFiles()
                }
        }
    }

    private fun refreshForOpenedFiles() {
        val files = PlsAnalysisManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsAnalysisManager.refreshFiles(files)
    }
}

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
                .onApply { refreshForAllOpenFiles() }
        }
    }

    private fun refreshForAllOpenFiles() {
        // 刷新所有已打开的文件
        val files = PlsAnalysisManager.findAllOpenFiles()
        PlsAnalysisManager.refreshFiles(files)
    }
}

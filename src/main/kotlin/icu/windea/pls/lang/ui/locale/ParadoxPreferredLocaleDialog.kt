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
            val settings = getSettings()
            val oldPreferredLocale = settings.preferredLocale
            localeComboBox(withAuto = true).bindItem(settings::preferredLocale.toNullableProperty())
                .onApply {
                    if (oldPreferredLocale != settings.preferredLocale) {
                        //刷新已打开的文件
                        val files = PlsManager.findOpenedFiles()
                        PlsManager.reparseAndRefreshFiles(files, reparse = false)
                    }
                }
        }
    }
}

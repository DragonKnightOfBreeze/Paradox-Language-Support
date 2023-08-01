package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.cwt.config.*

class ParadoxLocaleCheckboxDialog(
    selectedLocales: List<CwtLocalisationLocaleConfig>,
    localesToSelect: List<CwtLocalisationLocaleConfig> = getLocalesToSelect()
): DialogWrapper(null, false) {
    val localeStatusMap = localesToSelect.associateWithTo(mutableMapOf()) { it in selectedLocales }
    
    init {
        title = PlsBundle.message("ui.dialog.selectLocales.title")
        init()
    }
    
    override fun createCenterPanel() = panel {
        localeStatusMap.keys.forEach { locale ->
            row {
                checkBox(locale.description).bindSelected({ localeStatusMap[locale] ?: false }, { localeStatusMap[locale] = it })
            }
        }
    }
}

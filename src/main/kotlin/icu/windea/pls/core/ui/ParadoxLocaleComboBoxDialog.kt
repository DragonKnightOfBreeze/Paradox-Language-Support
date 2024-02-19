@file:Suppress("CanBeParameter")

package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

class ParadoxLocaleComboBoxDialog(
    val selectedLocale: CwtLocalisationLocaleConfig?,
    val allLocales: Collection<CwtLocalisationLocaleConfig>
) : DialogWrapper(null, false) {
    var locale: CwtLocalisationLocaleConfig? = selectedLocale
    
    init {
        title = PlsBundle.message("ui.dialog.selectLocale.title")
        init()
    }
    
    override fun createCenterPanel() = panel {
        row {
            comboBox(allLocales).bindItem(::locale.toNullableProperty()).focused()
        }
    }
}


@file:Suppress("CanBeParameter")

package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

class ParadoxLocaleComboBoxDialog(
    val selectedLocale: CwtLocaleConfig?,
    val allLocales: Collection<CwtLocaleConfig>
) : DialogWrapper(null, false) {
    var locale: CwtLocaleConfig? = selectedLocale

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

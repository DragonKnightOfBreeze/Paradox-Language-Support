@file:Suppress("CanBeParameter")

package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

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

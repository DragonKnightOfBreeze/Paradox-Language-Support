package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

@Suppress("unused")
class ParadoxLocaleComboBoxDialog(
    val allLocales: Collection<CwtLocaleConfig>,
    val selectedLocale: CwtLocaleConfig?,
) : DialogWrapper(null, false) {
    var locale: CwtLocaleConfig? = selectedLocale

    init {
        title = PlsBundle.message("ui.selectLocale.title")
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            comboBox(allLocales).bindItem(::locale.toNullableProperty()).focused()
        }
    }
}

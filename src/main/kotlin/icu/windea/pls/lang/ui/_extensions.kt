package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

fun Row.localeComboBox(allLocales: Collection<CwtLocaleConfig>): Cell<ComboBox<String>> {
    val localeMap = allLocales.associateBy { it.name }
    return comboBox(localeMap.keys, textListCellRenderer { it?.let { s -> localeMap[s]?.text ?: s } })
}

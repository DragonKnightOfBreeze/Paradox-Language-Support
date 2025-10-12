package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.lang.util.ParadoxLocaleManager

fun Row.localeComboBox(withAuto: Boolean = false, withDefault: Boolean = false, pingPreferred: Boolean = true): Cell<ComboBox<String>> {
    val locales = ParadoxLocaleManager.getLocaleConfigs(withAuto, withDefault, pingPreferred)
    val localeMap = locales.associateBy { it.id }

    return comboBox(localeMap.keys, textListCellRenderer { it?.let { s -> localeMap[s]?.text ?: s } })
}

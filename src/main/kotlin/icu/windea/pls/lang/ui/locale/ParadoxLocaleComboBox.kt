package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.lang.util.*

fun Row.localeComboBox(withAuto: Boolean = false, withDefault: Boolean = false, pingPreferred: Boolean = true): Cell<ComboBox<String>> {
    val locales = ParadoxLocaleManager.getLocaleConfigs(withAuto, withDefault, pingPreferred)
    val localeMap = locales.associateBy { it.id }

    return comboBox(localeMap.keys, textListCellRenderer { it?.let { s -> localeMap[s]?.description ?: s } })
}

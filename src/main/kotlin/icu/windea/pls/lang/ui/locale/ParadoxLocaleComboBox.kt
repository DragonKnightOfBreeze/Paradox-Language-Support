package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import icu.windea.pls.lang.util.*

fun Row.localeComboBox(withAuto: Boolean = false, withDefault: Boolean = false, pingPreferred: Boolean = true): Cell<ComboBox<String>> {
    val locales = ParadoxLocaleManager.getLocaleConfigs(withAuto, withDefault, pingPreferred)
    val localeMap = locales.associateBy { it.id }

    return comboBox(localeMap.keys, SimpleListCellRenderer.create { label, value, _ -> label.text = localeMap[value]?.description ?: value })
}

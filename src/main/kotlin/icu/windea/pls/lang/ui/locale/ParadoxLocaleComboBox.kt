package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import icu.windea.pls.*
import icu.windea.pls.lang.util.*

fun Row.localeComboBox(addAuto: Boolean = false): Cell<ComboBox<String>> {
    val localeList = buildList {
        if(addAuto) add("auto")
        addAll(ParadoxLocaleManager.getLocaleConfigs(pingPreferred = false).map { it.id })
    }
    return comboBox(localeList, SimpleListCellRenderer.create { label, value: String, _ ->
        when(value) {
            "auto" -> label.text = PlsBundle.message("locale.auto")
            else -> label.text = ParadoxLocaleManager.getLocaleConfig(value).description
        }
    })
}

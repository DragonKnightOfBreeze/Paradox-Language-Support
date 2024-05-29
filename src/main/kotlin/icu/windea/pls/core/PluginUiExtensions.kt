package icu.windea.pls.core

import com.intellij.openapi.ui.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.util.*

fun Row.localeComboBox(addDefault: Boolean = false, addAuto: Boolean = false): Cell<ComboBox<String>> {
    val localeList = buildList {
        if(addDefault) add("")
        if(addAuto) add("auto")
        addAll(ParadoxLocaleHandler.getLocaleConfigMapById(pingPreferred = false).keys)
    }
    return comboBox(localeList, SimpleListCellRenderer.create { label, value: String, _ ->
        when(value) {
            "" -> label.text = PlsBundle.message("locale.default")
            "auto" -> label.text = PlsBundle.message("locale.auto")
            else -> label.text = getConfigGroup(null).localisationLocalesById.getValue(value).description
        }
    })
}
package icu.windea.pls.core

import com.intellij.openapi.ui.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.settings.*

fun Row.localeComboBox(settings: ParadoxSettingsState): Cell<ComboBox<String>> {
    return comboBox(settings.localeList, SimpleListCellRenderer.create { label, value: String, _ ->
        if(value == "auto") {
            label.text = PlsBundle.message("locale.auto")
        } else {
            label.text = getConfigGroup(null).localisationLocalesById.getValue(value).description
        }
    })
}
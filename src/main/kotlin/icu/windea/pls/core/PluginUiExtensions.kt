package icu.windea.pls.core

import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.settings.*

fun Row.localeComboBox(settings: ParadoxSettingsState) =
    this.comboBox(settings.localeList, listCellRenderer { value ->
        if(value == "auto") {
            text = PlsBundle.message("locale.auto")
        } else {
            text = getConfigGroup(null).localisationLocalesById.getValue(value).description
        }
    })
package icu.windea.pls.core

import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.settings.*

fun Row.localeComboBox(settings: ParadoxSettingsState) =
    this.comboBox(settings.localeList, listCellRenderer { value ->
        if(value == "auto") {
            text = PlsBundle.message("locale.auto")
        } else {
            text = getConfigGroup(null).localisationLocalesById.getValue(value).description
        }
    })
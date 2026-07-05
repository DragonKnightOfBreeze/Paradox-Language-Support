package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.util.ParadoxLocaleManager

fun Row.localeComboBox(configGroup: CwtConfigGroup = ChronicleFacade.getConfigGroup(), includeAuto: Boolean = false, includeDefault: Boolean = false, pingPreferred: Boolean = true): Cell<ComboBox<String>> {
    val locales = ParadoxLocaleManager.getGlobalLocales(configGroup, includeAuto, includeDefault, pingPreferred)
    return localeComboBox(locales)
}

fun Row.localeComboBox(allLocales: Collection<CwtLocaleConfig>): Cell<ComboBox<String>> {
    val localeMap = allLocales.associateBy { it.id }
    return comboBox(localeMap.keys, textListCellRenderer { it?.let { s -> localeMap[s]?.text ?: s } })
}

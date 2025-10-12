package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

@Suppress("CanBeParameter")
class ParadoxLocaleCheckBoxDialog(
    val allLocales: Collection<CwtLocaleConfig>,
    val selectedLocales: Collection<CwtLocaleConfig>
) : DialogWrapper(null, false) {
    val localeStatusMap = allLocales.associateWithTo(mutableMapOf()) { it in selectedLocales }

    init {
        title = PlsBundle.message("ui.selectLocales.title")
        init()
    }

    override fun createCenterPanel() = panel {
        localeStatusMap.keys.forEach { locale ->
            row {
                checkBox(locale.text).bindSelected({ localeStatusMap[locale] ?: false }, { localeStatusMap[locale] = it })
            }
        }
    }
}

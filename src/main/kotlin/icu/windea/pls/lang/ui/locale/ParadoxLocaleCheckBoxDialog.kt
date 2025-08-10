@file:Suppress("CanBeParameter")

package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

class ParadoxLocaleCheckBoxDialog(
    val selectedLocales: Collection<CwtLocaleConfig>,
    val allLocales: Collection<CwtLocaleConfig>
) : DialogWrapper(null, false) {
    val localeStatusMap = allLocales.associateWithTo(mutableMapOf()) { it in selectedLocales }

    init {
        title = PlsBundle.message("ui.dialog.selectLocales.title")
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

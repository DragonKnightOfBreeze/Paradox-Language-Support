@file:Suppress("CanBeParameter")

package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

class ParadoxLocaleListPopup(
    val allLocales: List<CwtLocaleConfig>
) : BaseListPopupStep<CwtLocaleConfig>(PlsBundle.message("ui.popup.selectLocale.title"), allLocales) {
    var selectedLocale: CwtLocaleConfig? = null

    override fun getIconFor(value: CwtLocaleConfig) = PlsIcons.Nodes.LocalisationLocale

    override fun getTextFor(value: CwtLocaleConfig) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: CwtLocaleConfig, finalChoice: Boolean): PopupStep<*>? {
        selectedLocale = selectedValue
        return FINAL_CHOICE
    }
}

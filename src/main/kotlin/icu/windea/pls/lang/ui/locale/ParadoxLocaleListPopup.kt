@file:Suppress("CanBeParameter")

package icu.windea.pls.lang.ui.locale

import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

class ParadoxLocaleListPopup(
    val selectedLocale: CwtLocalisationLocaleConfig?,
    val allLocales: List<CwtLocalisationLocaleConfig>,
    private val onChosen: (selected: CwtLocalisationLocaleConfig) -> Unit
) : BaseListPopupStep<CwtLocalisationLocaleConfig>(PlsBundle.message("ui.popup.selectLocale.title"), allLocales) {
    override fun getIconFor(value: CwtLocalisationLocaleConfig) = if (value.id == "auto") null else PlsIcons.Nodes.LocalisationLocale

    override fun getTextFor(value: CwtLocalisationLocaleConfig) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: CwtLocalisationLocaleConfig, finalChoice: Boolean): PopupStep<*>? {
        onChosen(selectedValue)
        return FINAL_CHOICE
    }
}

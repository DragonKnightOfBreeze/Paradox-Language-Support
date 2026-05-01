package icu.windea.pls.lang.ui

import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.delegated.CwtLocaleConfig

@Suppress("CanBeParameter")
class ParadoxLocaleListPopup(
    val allLocales: List<CwtLocaleConfig>,
    val onSelected: (selectedValue: CwtLocaleConfig) -> Unit = {},
) : BaseListPopupStep<CwtLocaleConfig>(PlsBundle.message("ui.selectLocale.title"), allLocales) {
    private var callback = onSelected

    override fun getIconFor(value: CwtLocaleConfig) = PlsIcons.Nodes.LocalisationLocale

    override fun getTextFor(value: CwtLocaleConfig) = value.idWithText

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: CwtLocaleConfig, finalChoice: Boolean) = doFinalStep { callback(selectedValue) }

    fun onSelected(onSelected: (selectedValue: CwtLocaleConfig) -> Unit) {
        callback = onSelected
    }
}

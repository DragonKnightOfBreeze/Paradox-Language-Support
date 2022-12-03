package icu.windea.pls.core.ui

import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.ext.*

class SelectParadoxLocalePopup(
	locale: CwtLocalisationLocaleConfig? = null,
	existingLocales: List<CwtLocalisationLocaleConfig> = emptyList(),
	localesToSelect: List<CwtLocalisationLocaleConfig> = getLocalesToSelect(existingLocales, locale),
	private val onChosen: (selected: CwtLocalisationLocaleConfig) -> Unit //必须传入，否则没有意义
) : BaseListPopupStep<CwtLocalisationLocaleConfig>(PlsBundle.message("ui.popup.selectParadoxLocale.title"), localesToSelect) {
	var locale: CwtLocalisationLocaleConfig? = null //初始为null
	
	override fun getIconFor(value: CwtLocalisationLocaleConfig) = value.icon
	
	override fun getTextFor(value: CwtLocalisationLocaleConfig) = value.text
	
	override fun isSpeedSearchEnabled() = true
	
	override fun onChosen(selectedValue: CwtLocalisationLocaleConfig, finalChoice: Boolean): PopupStep<*>? {
		onChosen(selectedValue)
		return FINAL_CHOICE
	}
}

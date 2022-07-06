package icu.windea.pls.core.ui

import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

class SelectParadoxLocalePopup(
	locale: ParadoxLocaleConfig? = null,
	existingLocales: List<ParadoxLocaleConfig> = emptyList(),
	localesToSelect: List<ParadoxLocaleConfig> = getLocalesToSelect(existingLocales, locale),
	private val onChosen: (selected: ParadoxLocaleConfig) -> Unit = {}
) : BaseListPopupStep<ParadoxLocaleConfig>(PlsBundle.message("ui.popup.selectParadoxLocale.title"), localesToSelect) {
	var locale: ParadoxLocaleConfig? = null //初始为null
	
	override fun getIconFor(value: ParadoxLocaleConfig) = value.icon
	
	override fun getTextFor(value: ParadoxLocaleConfig) = value.text
	
	override fun isSpeedSearchEnabled() = true
	
	override fun onChosen(selectedValue: ParadoxLocaleConfig, finalChoice: Boolean): PopupStep<*>? {
		onChosen(selectedValue)
		return FINAL_CHOICE
	}
}
package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.ext.*

class SelectParadoxLocaleDialog(
	locale: CwtLocalisationLocaleConfig? = null,
	existingLocales: List<CwtLocalisationLocaleConfig> = emptyList(),
	private val localesToSelect: List<CwtLocalisationLocaleConfig> = getLocalesToSelect(existingLocales, locale)
) : DialogWrapper(null, false) {
	var locale = locale ?: localesToSelect.firstOrNull()
	
	init {
		title = PlsBundle.message("ui.dialog.selectParadoxLocale.title")
		init()
	}
	
	override fun createCenterPanel() = panel {
		row {
			comboBox(localesToSelect).bindItem(::locale.toNullableProperty()).focused()
		}
	}
}

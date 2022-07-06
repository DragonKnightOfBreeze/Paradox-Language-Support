package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import javax.swing.*

class SelectParadoxLocaleDialog(
	locale: ParadoxLocaleConfig? = null,
	existingLocales: List<ParadoxLocaleConfig> = emptyList(),
	private val localesToSelect: List<ParadoxLocaleConfig> = getLocalesToSelect(existingLocales, locale)
) : DialogWrapper(null, false) {
	var locale = locale ?: localesToSelect.firstOrNull()
	
	init {
		title = PlsBundle.message("ui.dialog.selectParadoxLocale.title")
		init()
	}
	
	override fun createCenterPanel(): JComponent {
		return panel {
			row {
				comboBox(localesToSelect).bindItem(::locale.toNullableProperty()).focused()
			}
		}
	}
}
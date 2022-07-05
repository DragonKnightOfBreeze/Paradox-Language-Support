package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import javax.swing.*

class SelectParadoxLocaleDialog(
	locale: ParadoxLocaleConfig? = null,
	existingLocales: List<ParadoxLocaleConfig> = emptyList()
): DialogWrapper(null, false) {
	val localesToSelect = if(existingLocales.isEmpty()) {
		InternalConfigHandler.getLocaleMap().values
	} else{
		InternalConfigHandler.getLocaleMap().values.filter { it == locale || it !in existingLocales }
	}
	var locale = locale ?: InternalConfigHandler.getLocaleList().first()
	
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
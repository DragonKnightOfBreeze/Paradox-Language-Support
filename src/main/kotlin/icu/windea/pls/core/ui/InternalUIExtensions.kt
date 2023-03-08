package icu.windea.pls.core.ui

import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*

internal fun getLocalesToSelect(existingLocales: List<CwtLocalisationLocaleConfig>, locale: CwtLocalisationLocaleConfig?): List<CwtLocalisationLocaleConfig> {
	//置顶偏好的语言区域
	val preferredLocale = preferredParadoxLocale()
	val allLocaleConfigs = getCwtConfig().core.localisationLocales
	return if(existingLocales.isEmpty()) {
		allLocaleConfigs.values.pinned { it == preferredLocale }
	} else {
		allLocaleConfigs.values.filter { it == locale || it !in existingLocales }.pinned { it == preferredLocale }
	}
}

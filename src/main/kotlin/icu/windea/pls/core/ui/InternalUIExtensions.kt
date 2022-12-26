package icu.windea.pls.core.ui

import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.config.ext.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

internal fun getLocalesToSelect(existingLocales: List<CwtLocalisationLocaleConfig>, locale: CwtLocalisationLocaleConfig?): List<CwtLocalisationLocaleConfig> {
	//置顶偏好的语言区域
	val preferredLocale = preferredParadoxLocale()
	val locales = getCwtConfig().core.localisationLocales.values
	return if(existingLocales.isEmpty()) {
		locales.pinned { it == preferredLocale }
	} else {
		locales.filter { it == locale || it !in existingLocales }.pinned { it == preferredLocale }
	}
}

package icu.windea.pls.core.ui

import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

internal fun getLocalesToSelect(existingLocales: List<ParadoxLocaleConfig>, locale: ParadoxLocaleConfig?): List<ParadoxLocaleConfig> {
	//置顶偏好的语言区域
	val preferredLocale = preferredParadoxLocale()
	return if(existingLocales.isEmpty()) {
		InternalConfigHandler.getLocaleMap(includeDefault = false).values.pinned { it == preferredLocale }
	} else {
		InternalConfigHandler.getLocaleMap(includeDefault = false).values.filter { it == locale || it !in existingLocales }.pinned { it == preferredLocale }
	}
}
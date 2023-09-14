package icu.windea.pls.lang

import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

object ParadoxLocaleHandler {
    fun getPreferredLocale(): CwtLocalisationLocaleConfig {
        return getLocale(getSettings().preferredLocale.orEmpty())
    }
    
    fun getLocale(localeString: String): CwtLocalisationLocaleConfig {
        //基于localeString得到对应的语言区域
        if(localeString.isNotEmpty() && localeString != "auto") {
            val localesById = getConfigGroups().core.localisationLocalesById
            val locale = localesById.get(localeString)
            if(locale != null) return locale
        }
        //基于OS得到对应的语言区域，或者使用英文
        val userLanguage = System.getProperty("user.language") ?: "en"
        val localesByCode = getConfigGroups().core.localisationLocalesByCode
        return localesByCode.get(userLanguage) ?: localesByCode.get("en") ?: throw IllegalStateException()
    }
    
    fun getLocaleConfigs(pingPreferred: Boolean = true, noDefault: Boolean = true): List<CwtLocalisationLocaleConfig> {
        var locales = getConfigGroups().core.localisationLocalesById.values
        if(pingPreferred) {
            val preferredLocale = getPreferredLocale()
            locales = locales.pinned { it == preferredLocale }
        }
        if(noDefault) {
            locales = locales.filter { it.id != "l_default" }
        }
        return locales.toListOrThis()
    }
    
    fun getLocaleConfigMapById(pingPreferred: Boolean = true, noDefault: Boolean = true): Map<String, CwtLocalisationLocaleConfig> {
        return getLocaleConfigs(pingPreferred, noDefault).associateBy { it.id }
    }
    
    fun getLocaleConfigMapByShortId(pingPreferred: Boolean = true, noDefault: Boolean = true): Map<String, CwtLocalisationLocaleConfig> {
        return getLocaleConfigs(pingPreferred, noDefault).associateBy { it.id.removePrefix("l_") }
    }
}
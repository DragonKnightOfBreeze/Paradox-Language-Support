package icu.windea.pls.lang

import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.cwt.config.*

object ParadoxLocaleHandler {
    fun getPreferredLocale(): CwtLocalisationLocaleConfig {
        return ParadoxLocaleHandler.getLocale(getSettings().preferredLocale.orEmpty())
    }
    
    fun getLocale(locale: String): CwtLocalisationLocaleConfig {
        val primaryLocale = locale
        if(primaryLocale.isNotEmpty() && primaryLocale != "auto") {
            val locales = getCwtConfig().core.localisationLocales
            val usedLocale = locales.get(primaryLocale)
            if(usedLocale != null) return usedLocale
        }
        //基于OS得到对应的语言区域，或者使用英文
        val userLanguage = System.getProperty("user.language") ?: "en"
        val locales = getLocaleConfigMapByCode()
        return locales.get(userLanguage) ?: locales.get("en") ?: throw IllegalStateException()
    }
    
    fun getLocaleConfigs(pingPreferred: Boolean = true, noDefault: Boolean = true): List<CwtLocalisationLocaleConfig> {
        var localeConfigs = getCwtConfig().core.localisationLocales.values
        if(pingPreferred) {
            val preferredLocale = getPreferredLocale()
            localeConfigs = localeConfigs.pinned { it == preferredLocale }
        }
        if(noDefault) {
            localeConfigs = localeConfigs.filter { it.id != "l_default" }
        }
        return localeConfigs.toListOrThis()
    }
    
    fun getLocaleConfigMapById(pingPreferred: Boolean = true, noDefault: Boolean = true): Map<String, CwtLocalisationLocaleConfig> {
        return getLocaleConfigs(pingPreferred, noDefault).associateBy { it.id }
    }
    
    fun getLocaleConfigMapByShortId(pingPreferred: Boolean = true, noDefault: Boolean = true): Map<String, CwtLocalisationLocaleConfig> {
        return getLocaleConfigs(pingPreferred, noDefault).associateBy { it.id.removePrefix("l_") }
    }
    
    fun getLocaleConfigMapByCode(pingPreferred: Boolean = true, noDefault: Boolean = true): Map<String, CwtLocalisationLocaleConfig> {
        return buildMap { getLocaleConfigs(pingPreferred, noDefault).forEach { it.codes.forEach { code -> put(code, it) } } }
    }
}
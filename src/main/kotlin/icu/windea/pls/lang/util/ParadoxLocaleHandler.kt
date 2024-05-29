package icu.windea.pls.lang.util

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

object ParadoxLocaleHandler {
    fun getPreferredLocale(): CwtLocalisationLocaleConfig {
        return getLocale(getSettings().preferredLocale.orEmpty())
    }
    
    fun getUsedLocaleInDocumentation(): CwtLocalisationLocaleConfig? {
        val localeInSettings = getSettings().documentation.locale?.orNull()
        if(localeInSettings == null) return null
        return getLocale(localeInSettings)
    }
    
    fun getLocale(localeString: String): CwtLocalisationLocaleConfig {
        //基于localeString得到对应的语言区域
        if(localeString.isNotEmpty() && localeString != "auto") {
            val localesById = getConfigGroup(null).localisationLocalesById
            val locale = localesById.get(localeString)
            if(locale != null) return locale
        }
        //基于OS得到对应的语言区域，或者使用英文
        val userLanguage = System.getProperty("user.language") ?: "en"
        val localesByCode = getConfigGroup(null).localisationLocalesByCode
        return localesByCode.get(userLanguage) ?: localesByCode.get("en") ?: throw IllegalStateException()
    }
    
    fun getLocaleConfigs(pingPreferred: Boolean = true, noDefault: Boolean = true): List<CwtLocalisationLocaleConfig> {
        var locales: Collection<CwtLocalisationLocaleConfig> = getConfigGroup(null).localisationLocalesById.values
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
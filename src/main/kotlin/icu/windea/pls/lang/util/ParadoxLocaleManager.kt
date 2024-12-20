package icu.windea.pls.lang.util

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*

object ParadoxLocaleManager {
    fun getPreferredLocaleConfig(): CwtLocalisationLocaleConfig {
        return getLocaleConfig(getSettings().preferredLocale.orEmpty())
    }

    fun getLocaleInDocumentation(element: PsiElement): CwtLocalisationLocaleConfig? {
        val cache = element.getUserData(PlsKeys.documentationLocale) ?: return null
        val localeFromCache = when {
            cache == "auto" -> CwtLocalisationLocaleConfig.AUTO
            else -> getLocaleConfigById(cache)
        }
        return localeFromCache
    }

    fun getUsedLocaleInDocumentation(element: PsiElement, defaultLocale: CwtLocalisationLocaleConfig? = null): CwtLocalisationLocaleConfig {
        val cache = element.getOrPutUserData(PlsKeys.documentationLocale) { defaultLocale?.id ?: "auto" }
        val localeFromCache = when {
            cache == "auto" -> getPreferredLocaleConfig()
            else -> getLocaleConfigById(cache) ?: defaultLocale ?: getPreferredLocaleConfig()
        }
        return localeFromCache
    }

    fun getLocaleConfig(localeString: String): CwtLocalisationLocaleConfig {
        //基于localeString得到对应的语言区域
        if (localeString.isNotEmpty() && localeString != "auto") {
            val localesById = getConfigGroup(null).localisationLocalesById
            val locale = localesById.get(localeString)
            if (locale != null) return locale
        }
        //基于OS得到对应的语言区域，或者使用英文
        val userLanguage = System.getProperty("user.language") ?: "en"
        val localesByCode = getConfigGroup(null).localisationLocalesByCode
        return localesByCode.get(userLanguage) ?: localesByCode.get("en") ?: throw IllegalStateException()
    }

    fun getLocaleConfigById(id: String): CwtLocalisationLocaleConfig? {
        return getConfigGroup(null).localisationLocalesById[id]
    }

    fun getLocaleConfigs(pingPreferred: Boolean = true, noDefault: Boolean = true): List<CwtLocalisationLocaleConfig> {
        var locales: Collection<CwtLocalisationLocaleConfig> = getConfigGroup(null).localisationLocalesById.values
        if (pingPreferred) {
            val preferredLocale = getPreferredLocaleConfig()
            locales = locales.pinned { it == preferredLocale }
        }
        if (noDefault) {
            locales = locales.filter { it.id != "l_default" }
        }
        return locales.toListOrThis()
    }

}

package icu.windea.pls.lang.util

import com.intellij.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*

object ParadoxLocaleManager {
    const val ID_AUTO = "auto"
    const val ID_AUTO_OS = "auto.os"
    const val ID_DEFAULT = "l_default"
    const val ID_FALLBACK = "l_english"

    fun getPreferredLocaleConfig(): CwtLocalisationLocaleConfig {
        return getResolvedLocaleConfig(PlsFacade.getSettings().preferredLocale.orEmpty()) ?: CwtLocalisationLocaleConfig.FALLBACK
    }

    fun getResolvedLocaleConfig(id: String): CwtLocalisationLocaleConfig? {
        val localesById = PlsFacade.getConfigGroup(null).localisationLocalesById
        val locale = localesById[id]
        if (locale != null) return locale

        return when {
            id.isEmpty() || id == ID_AUTO -> {
                val ideLocale = DynamicBundle.getLocale()
                val localesByCode = PlsFacade.getConfigGroup(null).localisationLocalesByCode
                localesByCode[ideLocale.language] ?: CwtLocalisationLocaleConfig.FALLBACK
            }
            id == ID_AUTO_OS -> {
                val userLanguage = System.getProperty("user.language").orEmpty()
                val localesByCode = PlsFacade.getConfigGroup(null).localisationLocalesByCode
                localesByCode[userLanguage] ?: CwtLocalisationLocaleConfig.FALLBACK
            }
            else -> null
        }
    }

    fun getLocaleConfig(id: String, withAuto: Boolean = false, withDefault: Boolean = false): CwtLocalisationLocaleConfig? {
        if (withAuto) {
            if (id == ID_AUTO) return CwtLocalisationLocaleConfig.AUTO
            if (id == ID_AUTO_OS) return CwtLocalisationLocaleConfig.AUTO_OS
        }
        val localesById = PlsFacade.getConfigGroup(null).localisationLocalesById
        val locale = localesById[id] ?: return null
        if (!withDefault) {
            if (locale.id == ID_DEFAULT) return null
        }
        return locale
    }

    fun getLocaleConfigs(withAuto: Boolean = false, withDefault: Boolean = false, pingPreferred: Boolean = true): List<CwtLocalisationLocaleConfig> {
        val locales = mutableListOf<CwtLocalisationLocaleConfig>()
        if (withAuto) {
            locales += CwtLocalisationLocaleConfig.AUTO
            locales += CwtLocalisationLocaleConfig.AUTO_OS
        }
        val localesById = PlsFacade.getConfigGroup(null).localisationLocalesById
        var locales0 = localesById.values.toList()
        if (!withDefault) {
            locales0 = locales0.filter { it.id != ID_DEFAULT }
        }
        if (pingPreferred) {
            val preferredLocale = getPreferredLocaleConfig()
            locales0 = locales0.pinned { it == preferredLocale }
        }
        locales += locales0
        return locales
    }

    fun getLocaleConfigInDocumentation(element: PsiElement): CwtLocalisationLocaleConfig? {
        val id = element.getUserData(PlsKeys.documentationLocale) ?: return null
        val locale = getLocaleConfig(id, withAuto = true)
        return locale
    }

    fun getResolvedLocaleConfigInDocumentation(element: PsiElement, defaultLocale: CwtLocalisationLocaleConfig? = null): CwtLocalisationLocaleConfig {
        val id = element.getUserData(PlsKeys.documentationLocale)
        val locale = id?.let { getResolvedLocaleConfig(id) } ?: defaultLocale ?: getPreferredLocaleConfig()
        return locale
    }
}

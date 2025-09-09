package icu.windea.pls.lang.util

import com.intellij.DynamicBundle
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configGroup.localisationLocalesByCode
import icu.windea.pls.config.configGroup.localisationLocalesById
import icu.windea.pls.core.collections.pinned
import icu.windea.pls.lang.PlsKeys

object ParadoxLocaleManager {
    const val ID_AUTO = "auto"
    const val ID_AUTO_OS = "auto.os"
    const val ID_DEFAULT = "l_default"
    const val ID_FALLBACK = "l_english"

    fun getPreferredLocaleConfig(): CwtLocaleConfig {
        return getResolvedLocaleConfig(PlsFacade.getSettings().preferredLocale.orEmpty()) ?: CwtLocaleConfig.resolveFallback()
    }

    fun getResolvedLocaleConfig(id: String): CwtLocaleConfig? {
        val localesById = PlsFacade.getConfigGroup().localisationLocalesById
        val locale = localesById[id]
        if (locale != null) return locale

        return when {
            id.isEmpty() || id == ID_AUTO -> {
                val ideLocale = DynamicBundle.getLocale()
                val localesByCode = PlsFacade.getConfigGroup().localisationLocalesByCode
                localesByCode[ideLocale.language] ?: CwtLocaleConfig.resolveFallback()
            }
            id == ID_AUTO_OS -> {
                val userLanguage = System.getProperty("user.language").orEmpty()
                val localesByCode = PlsFacade.getConfigGroup().localisationLocalesByCode
                localesByCode[userLanguage] ?: CwtLocaleConfig.resolveFallback()
            }
            else -> null
        }
    }

    fun getLocaleConfig(id: String, withAuto: Boolean = false, withDefault: Boolean = false): CwtLocaleConfig? {
        if (withAuto) {
            if (id == ID_AUTO) return CwtLocaleConfig.resolveAuto()
            if (id == ID_AUTO_OS) return CwtLocaleConfig.resolveAutoOs()
        }
        val localesById = PlsFacade.getConfigGroup().localisationLocalesById
        val locale = localesById[id] ?: return null
        if (!withDefault) {
            if (locale.id == ID_DEFAULT) return null
        }
        return locale
    }

    fun getLocaleConfigs(withAuto: Boolean = false, withDefault: Boolean = false, pingPreferred: Boolean = true): List<CwtLocaleConfig> {
        val locales = mutableListOf<CwtLocaleConfig>()
        if (withAuto) {
            locales += CwtLocaleConfig.resolveAuto()
            locales += CwtLocaleConfig.resolveAutoOs()
        }
        val localesById = PlsFacade.getConfigGroup().localisationLocalesById
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

    fun getLocaleConfigInDocumentation(element: PsiElement): CwtLocaleConfig? {
        val id = element.getUserData(PlsKeys.documentationLocale) ?: return null
        val locale = getLocaleConfig(id, withAuto = true)
        return locale
    }

    fun getResolvedLocaleConfigInDocumentation(element: PsiElement, defaultLocale: CwtLocaleConfig? = null): CwtLocaleConfig {
        val id = element.getUserData(PlsKeys.documentationLocale)
        val locale = id?.let { getResolvedLocaleConfig(id) } ?: defaultLocale ?: getPreferredLocaleConfig()
        return locale
    }
}

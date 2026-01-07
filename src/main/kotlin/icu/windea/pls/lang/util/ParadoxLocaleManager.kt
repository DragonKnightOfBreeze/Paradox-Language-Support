package icu.windea.pls.lang.util

import com.intellij.DynamicBundle
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.pinned
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.settings.PlsSettings

object ParadoxLocaleManager {
    object Keys : KeyRegistry() {
        /** 用于标记快速文档使用的本地化语言环境。 */
        val documentationLocale by registerKey<String>(this)
    }

    const val ID_AUTO = "auto"
    const val ID_AUTO_OS = "auto.os"
    const val ID_DEFAULT = "l_default"
    const val ID_FALLBACK = "l_english"

    fun getPreferredLocaleConfig(): CwtLocaleConfig {
        return getResolvedLocaleConfig(PlsSettings.getInstance().state.preferredLocale.orEmpty()) ?: CwtLocaleConfig.resolveFallback()
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

    fun getResolvedLocaleConfigInDocumentation(element: PsiElement, defaultLocale: CwtLocaleConfig? = null): CwtLocaleConfig {
        val id = element.getUserData(Keys.documentationLocale)
        val locale = id?.let { getResolvedLocaleConfig(id) } ?: defaultLocale ?: getPreferredLocaleConfig()
        return locale
    }

    fun getLocaleConfigInDocumentation(element: PsiElement): CwtLocaleConfig? {
        val id = element.getUserData(Keys.documentationLocale) ?: return null
        val locale = getLocaleConfig(id, withAuto = true)
        return locale
    }

    fun getLocaleConfigById(element: PsiElement, id: String): CwtLocaleConfig? {
        return PlsFacade.getConfigGroup(element.project).localisationLocalesById.get(id)
    }
}

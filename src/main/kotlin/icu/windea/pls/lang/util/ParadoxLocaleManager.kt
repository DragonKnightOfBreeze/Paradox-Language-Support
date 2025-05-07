package icu.windea.pls.lang.util

import com.intellij.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*

object ParadoxLocaleManager {
    const val ID_AUTO = "auto"
    const val ID_AUTO_OS = "auto.os"
    const val ID_DEFAULT = "l_default"

    fun getPreferredLocaleConfig(): CwtLocalisationLocaleConfig {
        return resolveLocaleConfig(getSettings().preferredLocale.orEmpty()) ?: getLocaleConfig("en") ?: throw IllegalStateException()
    }

    fun resolveLocaleConfig(id: String): CwtLocalisationLocaleConfig? {
        val localesById = getConfigGroup(null).localisationLocalesById
        val locale = localesById[id]
        if (locale != null) return locale

        return when {
            id.isEmpty() || id == ID_AUTO -> {
                val ideLocale = DynamicBundle.getLocale()
                val localesByCode = getConfigGroup(null).localisationLocalesByCode
                localesByCode[ideLocale.language] ?: localesByCode["en"]
            }
            id == ID_AUTO_OS -> {
                val userLanguage = System.getProperty("user.language") ?: "en"
                val localesByCode = getConfigGroup(null).localisationLocalesByCode
                localesByCode[userLanguage] ?: localesByCode["en"]
            }
            else -> null
        }
    }

    fun getLocaleConfig(id: String, withAuto: Boolean = false, withDefault: Boolean = false): CwtLocalisationLocaleConfig? {
        if (withAuto) {
            if (id == ID_AUTO) return CwtLocalisationLocaleConfig.AUTO
            if (id == ID_AUTO_OS) return CwtLocalisationLocaleConfig.AUTO_OS
        }
        val localesById = getConfigGroup(null).localisationLocalesById
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
        val localesById = getConfigGroup(null).localisationLocalesById
        localesById.forEach { (_, v) -> locales += v }
        if (!withDefault) {
            locales.removeIf { it.id == ID_DEFAULT }
        }
        if (pingPreferred) {
            val preferredLocale = getPreferredLocaleConfig()
            return locales.pinned { it == preferredLocale }
        }
        return locales
    }

    fun getLocaleInDocumentation(element: PsiElement): CwtLocalisationLocaleConfig? {
        val id = element.getUserData(PlsKeys.documentationLocale) ?: return null
        val locale = getLocaleConfig(id)
        return locale
    }

    fun getUsedLocaleInDocumentation(element: PsiElement, defaultLocale: CwtLocalisationLocaleConfig? = null): CwtLocalisationLocaleConfig {
        val id = element.getOrPutUserData(PlsKeys.documentationLocale) { defaultLocale?.id ?: ID_AUTO }
        val locale = resolveLocaleConfig(id) ?: defaultLocale ?: getPreferredLocaleConfig()
        return locale
    }
}

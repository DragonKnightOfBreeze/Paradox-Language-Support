package icu.windea.pls.lang.util

import com.intellij.DynamicBundle
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.pinned
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.settings.ChronicleSettings

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
        return getResolvedLocaleConfig(ChronicleSettings.getInstance().state.preferredLocale.orEmpty()) ?: CwtLocaleConfig.resolveFallback()
    }

    fun getResolvedLocaleConfig(id: String): CwtLocaleConfig? {
        val configGroup = ChronicleFacade.getConfigGroup()

        val locales = configGroup.locales
        val locale = locales[id]
        if (locale != null) return locale

        return when {
            id.isEmpty() || id == ID_AUTO -> {
                val ideLocale = DynamicBundle.getLocale()
                val code = ideLocale.language
                locales.values.find { code in it.codes } ?: CwtLocaleConfig.resolveFallback()
            }
            id == ID_AUTO_OS -> {
                val userLanguage = System.getProperty("user.language").orEmpty()
                val code = userLanguage
                locales.values.find { code in it.codes } ?: CwtLocaleConfig.resolveFallback()
            }
            else -> null
        }
    }

    fun getLocaleConfig(id: String, includeAuto: Boolean = false, includeDefault: Boolean = false): CwtLocaleConfig? {
        if (includeAuto) {
            if (id == ID_AUTO) return CwtLocaleConfig.resolveAuto()
            if (id == ID_AUTO_OS) return CwtLocaleConfig.resolveAutoOs()
        }
        val configGroup = ChronicleFacade.getConfigGroup()
        val locale = configGroup.locales[id] ?: return null
        if (!includeDefault) {
            if (locale.name == ID_DEFAULT) return null
        }
        return locale
    }

    fun getResolvedLocaleConfigInDocumentation(element: PsiElement, defaultLocale: CwtLocaleConfig? = null): CwtLocaleConfig {
        val id = element.getUserData(Keys.documentationLocale)
        val locale = id?.let { getResolvedLocaleConfig(id) } ?: defaultLocale ?: getPreferredLocaleConfig()
        return locale
    }

    fun getGlobalLocaleConfigInDocumentation(element: PsiElement): CwtLocaleConfig? {
        val id = element.getUserData(Keys.documentationLocale) ?: return null
        val locale = getLocaleConfig(id, includeAuto = true)
        return locale
    }

    fun getGlobalLocales(configGroup: CwtConfigGroup = ChronicleFacade.getConfigGroup(), includeAuto: Boolean = false, includeDefault: Boolean = false, pinPreferred: Boolean = true): List<CwtLocaleConfig> {
        return collectLocaleConfigs(configGroup.globalLocales, includeAuto, includeDefault, pinPreferred)
    }

    fun getSupportedLocales(configGroup: CwtConfigGroup = ChronicleFacade.getConfigGroup(), includeAuto: Boolean = false, includeDefault: Boolean = false, pinPreferred: Boolean = true): List<CwtLocaleConfig> {
        return collectLocaleConfigs(configGroup.supportedLocales, includeAuto, includeDefault, pinPreferred)
    }

    private fun collectLocaleConfigs(locales: List<CwtLocaleConfig>, includeAuto: Boolean, includeDefault: Boolean, pinPreferred: Boolean): List<CwtLocaleConfig> {
        val result = mutableListOf<CwtLocaleConfig>()
        if (includeAuto) {
            result += CwtLocaleConfig.resolveAuto()
            result += CwtLocaleConfig.resolveAutoOs()
        }
        var locales = locales
        if (!includeDefault) {
            locales = locales.filter { it.name != ID_DEFAULT }
        }
        if (pinPreferred) {
            val preferredLocale = getPreferredLocaleConfig()
            locales = locales.pinned { it == preferredLocale }
        }
        result += locales
        return result
    }
}

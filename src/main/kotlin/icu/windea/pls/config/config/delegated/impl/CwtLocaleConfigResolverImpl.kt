package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.lang.util.ParadoxLocaleManager

internal class CwtLocaleConfigResolverImpl : CwtLocaleConfig.Resolver {
    private val AUTO: CwtLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO)
    private val AUTO_OS: CwtLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO_OS)
    private val FALLBACK: CwtLocaleConfig = FallbackCwtLocaleConfig(ParadoxLocaleManager.ID_FALLBACK)

    override fun resolveAuto(): CwtLocaleConfig = AUTO
    override fun resolveAutoOs(): CwtLocaleConfig = AUTO_OS
    override fun resolveFallback(): CwtLocaleConfig = FALLBACK
    override fun resolve(config: CwtPropertyConfig): CwtLocaleConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocaleConfig {
        val id = config.key
        val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
        return CwtLocaleConfigImpl(config, id, codes)
    }
}

private class CwtLocaleConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val codes: List<String>
) : UserDataHolderBase(), CwtLocaleConfig {
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "CwtLocaleConfig(id='$id')"
}

private class AutoCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "AutoCwtLocaleConfig(id='$id')"
}

private class FallbackCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "FallbackCwtLocaleConfig(id='$id')"
}

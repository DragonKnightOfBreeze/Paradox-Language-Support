package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.util.ParadoxLocaleManager

internal class CwtLocaleConfigResolverImpl : CwtLocaleConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    private val autoLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO)
    private val autoOsLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO_OS)
    private val fallbackLocaleConfig = FallbackCwtLocaleConfig(ParadoxLocaleManager.ID_FALLBACK)

    override fun resolveAuto(): CwtLocaleConfig = autoLocaleConfig
    override fun resolveAutoOs(): CwtLocaleConfig = autoOsLocaleConfig
    override fun resolveFallback(): CwtLocaleConfig = fallbackLocaleConfig
    override fun resolve(config: CwtPropertyConfig): CwtLocaleConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocaleConfig {
        val id = config.key
        val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }?.optimized().orEmpty()
        logger.debug { "Resolved locale config (id: $id).".withLocationPrefix(config) }
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

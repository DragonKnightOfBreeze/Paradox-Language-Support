@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromProperty
import icu.windea.pls.config.config.delegated.impl.CwtLocaleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtLocaleConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val id: String
    @FromProperty("codes: string[]")
    val codes: List<String>

    val text: String
    val shortId: String get() = id.removePrefix("l_")
    val idWithText: String get() = if (text.isEmpty()) id else "$id ($text)"

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveAuto(): CwtLocaleConfig
        fun resolveAutoOs(): CwtLocaleConfig
        fun resolveFallback(): CwtLocaleConfig
        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig
    }

    companion object : Resolver by CwtLocaleConfigResolverImpl()
}

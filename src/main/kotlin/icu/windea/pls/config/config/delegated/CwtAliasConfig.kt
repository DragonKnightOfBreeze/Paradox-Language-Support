@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.impl.CwtAliasConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("alias[$:*]")
    val name: String
    @FromKey("alias[*:$]")
    val subName: String
    @FromOption("scope/scopes: string | string[]")
    val supportedScopes: Set<String>
    @FromOption("push_scope: string?")
    val outputScope: String?

    val subNameExpression: CwtDataExpression

    override val configExpression: CwtDataExpression get() = subNameExpression

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig?
    }

    companion object : Resolver by CwtAliasConfigResolverImpl()
}

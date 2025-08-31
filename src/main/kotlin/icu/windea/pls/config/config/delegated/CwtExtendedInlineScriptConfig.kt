@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromOption
import icu.windea.pls.config.config.delegated.impl.CwtExtendedInlineScriptConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

interface CwtExtendedInlineScriptConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String

    /**
     * 得到处理后的作为上下文规则的容器的规则。
     */
    fun getContainerConfig(): CwtMemberConfig<*>

    /**
     * 得到由其声明的上下文规则列表。
     */
    fun getContextConfigs(): List<CwtMemberConfig<*>>

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig
    }

    companion object : Resolver by CwtExtendedInlineScriptConfigResolverImpl()
}

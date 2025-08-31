@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromOption
import icu.windea.pls.config.config.delegated.impl.CwtExtendedParameterConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement

interface CwtExtendedParameterConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_key: string")
    val contextKey: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String
    @FromOption("inherit", defaultValue = "no")
    val inherit: Boolean

    /**
     * 得到处理后的作为上下文规则的容器的规则。
     */
    fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*>

    /**
     * 得到由其声明的上下文规则列表。
     */
    fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>>

    interface Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig?
    }

    companion object : Resolver by CwtExtendedParameterConfigResolverImpl()
}

@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

interface CwtSingleAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtSingleAliasConfig? {
    val key = config.key
    val name = key.removeSurroundingOrNull("single_alias[", "]")?.orNull()?.intern() ?: return null
    return CwtSingleAliasConfigImpl(config, name)
}

private class CwtSingleAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtSingleAliasConfig {
    override fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        //inline all value and configs
        val other = this.config
        val inlined = config.copy(
            value = other.value,
            valueType = other.valueType,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            optionConfigs = config.optionConfigs,
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.singleAliasConfig = this
        return inlined
    }

    override fun toString(): String {
        return "CwtSingleAliasConfigImpl(name='$name')"
    }
}

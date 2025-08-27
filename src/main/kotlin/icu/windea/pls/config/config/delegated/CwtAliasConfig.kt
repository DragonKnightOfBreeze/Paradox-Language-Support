@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.CwtConfig.*
import icu.windea.pls.config.configExpression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

interface CwtAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val subName: String
    @Option("scope/scopes: string | string[]")
    val supportedScopes: Set<String>
    @Option("push_scope: string?")
    val outputScope: String?

    val subNameExpression: CwtDataExpression
    override val configExpression: CwtDataExpression get() = subNameExpression

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtAliasConfig? {
    val key = config.key
    val tokens = key.removeSurroundingOrNull("alias[", "]")?.orNull()
        ?.split(':', limit = 2)?.takeIf { it.size == 2 }
        ?: return null
    val (name, subName) = tokens
    return CwtAliasConfigImpl(config, name, subName)
}

private class CwtAliasConfigImpl(
    override val config: CwtPropertyConfig,
    name: String,
    subName: String
) : UserDataHolderBase(), CwtAliasConfig {
    override val name = name.intern() //intern to optimize memory
    override val subName = subName.intern() //intern to optimize memory

    override val supportedScopes get() = config.supportedScopes
    override val outputScope get() = config.pushScope

    //not much memory will be used, so cached
    override val subNameExpression: CwtDataExpression = CwtDataExpression.resolve(subName, true)

    override fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        val other = this.config
        val inlined = config.copy(
            key = subName,
            value = other.value,
            valueType = other.valueType,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            optionConfigs = other.optionConfigs
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = this
        inlined.singleAliasConfig = config.singleAliasConfig
        return inlined
    }

    override fun toString(): String {
        return "CwtAliasConfigImpl(name='$name')"
    }
}


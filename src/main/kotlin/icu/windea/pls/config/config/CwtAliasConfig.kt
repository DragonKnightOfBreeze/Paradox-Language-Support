package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * @property name string
 * @property subName expression
 * @property supportedScopes (option) scope/scopes: string | string[]
 * @property outputScope (option) push_scope: string?
 */
interface CwtAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val subName: String
    val supportedScopes: Set<String>
    val outputScope: String?

    val subNameExpression: CwtDataExpression
    override val expression: CwtDataExpression get() = subNameExpression

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtAliasConfig? {
    val key = config.key
    val tokens = key.removeSurroundingOrNull("alias[", "]")?.orNull()
        ?.split(':', limit = 2)?.takeIf { it.size == 2 }
        ?: return null
    val (name, subName) = tokens
    return CwtAliasConfigImpl(config, name.intern(), subName.intern())
}

private class CwtAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val subName: String
) : UserDataHolderBase(), CwtAliasConfig {
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
            optionConfigs = other.optionConfigs,
            documentation = other.documentation,
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = this
        inlined.singleAliasConfig = config.singleAliasConfig
        return inlined
    }
}


package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*

/**
 * @property supportedScopes (option) scope/scopes: string | string[]
 */
class CwtAliasConfig(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val config: CwtPropertyConfig,
    override val name: String,
    val subName: String
) : CwtInlineableConfig<CwtProperty> {
    val subNameExpression = CwtKeyExpression.resolve(subName)
    override val expression get() = subNameExpression
    
    val supportedScopes get() = config.supportedScopes
    val outputScope get() = config.pushScope
    
    //private val inlinedConfigCache by lazy { ContainerUtil.createConcurrentSoftKeySoftValueMap<CwtPropertyConfig, CwtPropertyConfig>() }
    
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        return doInline(config)
    }
    
    private fun doInline(config: CwtPropertyConfig): CwtPropertyConfig {
        val other = this.config
        val inlined = config.copy(
            key = this.subName,
            value = other.value,
            configs = ParadoxConfigGenerator.deepCopyConfigs(other),
            documentation = other.documentation,
            options = other.options
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.inlineableConfig = config.inlineableConfig ?: this
        return inlined
    }
}


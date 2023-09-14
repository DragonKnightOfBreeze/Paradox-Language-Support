package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

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
    
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        val other = this.config
        val inlined = config.copy(
            key = subName,
            value = other.value,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            documentation = other.documentation,
            options = other.options
        )
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.inlineableConfig = config.inlineableConfig ?: this
        return inlined
    }
    
}


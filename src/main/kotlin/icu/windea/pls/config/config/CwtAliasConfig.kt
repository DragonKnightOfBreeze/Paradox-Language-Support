package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.cwt.psi.*

/**
 * @property supportedScopes (option) scope/scopes: string | string[]
 */
data class CwtAliasConfig(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val config: CwtPropertyConfig,
    override val name: String,
    val subName: String
) : CwtInlineableConfig<CwtProperty> {
    val subNameExpression = CwtKeyExpression.resolve(subName)
    
    override val expression get() = subNameExpression
    
    val supportedScopes get() = config.supportedScopes
    
    val outputScope get() = config.pushScope
}


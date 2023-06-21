package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
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
}


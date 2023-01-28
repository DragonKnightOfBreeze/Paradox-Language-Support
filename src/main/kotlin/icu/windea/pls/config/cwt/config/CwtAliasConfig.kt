package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
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
) : CwtInlineableConfig {
	val subNameExpression = CwtKeyExpression.resolve(subName)
	
	override val expression get() = subNameExpression
	
	val supportedScopes get() = config.supportedScopes
	
	val supportAnyScope get() = config.supportAnyScope
	
	val outputScope get() = config.pushScope
}


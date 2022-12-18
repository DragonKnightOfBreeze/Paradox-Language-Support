package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.cwt.psi.*

/**
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtLocalisationCommandConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val supportedScopes: Set<String>?
) : CwtConfig<CwtProperty> {
	val supportAnyScope = supportedScopes.isNullOrEmpty() || supportedScopes.singleOrNull().let { it == "all" }
	
	val supportedScopeNames: Set<String> by lazy {
		if(supportAnyScope) {
			setOf("Any")
		} else {
			supportedScopes?.mapTo(mutableSetOf()) { CwtConfigHandler.getScopeName(it, info.configGroup) }.orEmpty()
		}
	}
}
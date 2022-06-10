package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtLocalisationCommandConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty>{
	val supportedScopeNames: MutableSet<String> = mutableSetOf()
}
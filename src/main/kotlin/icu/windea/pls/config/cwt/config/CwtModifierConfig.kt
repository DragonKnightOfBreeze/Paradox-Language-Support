package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtModifierConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val name: String,
	val categories: Set<String>
) : CwtConfig<CwtProperty> {
	val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
	
	val supportedScopeNames: Set<String> by lazy { 
		categoryConfigMap.values.flatMapTo(mutableSetOf()) { it.supportedScopeNames }
	}
}
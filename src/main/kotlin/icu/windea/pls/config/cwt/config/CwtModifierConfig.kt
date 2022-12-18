package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @property supportedScopeNames 所有支持的作用域的名字。
 */
data class CwtModifierConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val categories: Set<String> //name or internal id
) : CwtConfig<CwtProperty> {
	val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
	
	val supportedScopeNames: Set<String> by lazy {
		val categoryConfigs = categoryConfigMap.values
		if(categoryConfigs.any { it.supportAnyScope }) {
			setOf("Any")
		} else {
			categoryConfigs.flatMapTo(mutableSetOf()) { it.supportedScopeNames }
		}
	}
}
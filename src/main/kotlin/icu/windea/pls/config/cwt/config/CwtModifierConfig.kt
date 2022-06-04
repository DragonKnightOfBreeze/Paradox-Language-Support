package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtModifierConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val categories: Set<String>
) : CwtConfig<CwtProperty> {
	val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
}
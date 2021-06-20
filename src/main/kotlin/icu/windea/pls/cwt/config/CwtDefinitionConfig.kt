package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

//TODO 这里排序可能出现问题

data class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val propertiesConfig: List<CwtPropertyConfig>,
	val subtypePropertiesConfig: Map<String, List<CwtPropertyConfig>>
) : icu.windea.pls.cwt.config.CwtConfig<CwtProperty> {
	fun mergeConfig(subtypes: List<String>): List<CwtPropertyConfig> {
		val result = mutableListOf<CwtPropertyConfig>()
		result.addAll(propertiesConfig)
		for((k, v) in subtypePropertiesConfig) {
			if(k in subtypes) result.addAll(v)
		}
		return result
	}
}


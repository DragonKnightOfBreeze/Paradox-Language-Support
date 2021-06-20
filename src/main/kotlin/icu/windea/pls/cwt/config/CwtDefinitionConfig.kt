package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

//TODO 这里排序可能出现问题

data class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val propertiesConfig: List<CwtPropertyConfig>,
	val subtypePropertiesConfig: Map<String, List<CwtPropertyConfig>>
) : CwtConfig<CwtProperty> {
	fun mergeConfig(subtypes: List<String>): List<CwtPropertyConfig> {
		val result = mutableListOf<CwtPropertyConfig>()
		result.addAll(propertiesConfig)
		for((k, v) in subtypePropertiesConfig) {
			if(k in subtypes) result.addAll(v)
		}
		return result
	}
	
	fun mergeAndDistinctConfig(subtypes: List<String>): List<CwtPropertyConfig> {
		val keys = hashSetOf<String>()
		val result = mutableListOf<CwtPropertyConfig>()
		for(c in propertiesConfig) {
			if(keys.add(c.key)) result.add(c)
		}
		for((k, v) in subtypePropertiesConfig) {
			if(k in subtypes) {
				for(c in v) {
					if(keys.add(c.key)) result.add(c)
				}
			}
		}
		return result
		//为了优化性能,不使用方法distinctBy
		//return mergeConfig(subtypes).distinctBy { it.key }
	}
}


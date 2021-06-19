package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

//TODO 这里排序可能出现问题

data class CwtDefinitionConfig(
	val name: String,
	val propertiesConfig: List<CwtConfigProperty>,
	val subtypePropertiesConfig: Map<String,List<CwtConfigProperty>>,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
) : CwtConfig{
	fun mergeConfig(subtypes:List<String>):List<CwtConfigProperty>{
		val result = mutableListOf<CwtConfigProperty>()
		result.addAll(propertiesConfig)
		for((k,v) in subtypePropertiesConfig) {
			if(k in subtypes) result.addAll(v)
		}
		return result
	}
}


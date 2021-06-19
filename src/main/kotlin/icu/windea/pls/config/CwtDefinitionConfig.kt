package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

//TODO 这里排序可能出现问题

data class CwtDefinitionConfig(
	val name: String,
	val propertiesConfig: Map<String, CwtConfigProperty>,
	val subtypePropertiesConfig: Map<String, Map<String, CwtConfigProperty>>,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
) : CwtConfig{
	fun mergeConfig(subtypes:List<String>):Map<String,CwtConfigProperty>{
		val config = mutableMapOf<String,CwtConfigProperty>()
		config.putAll(propertiesConfig)
		for((k,v) in subtypePropertiesConfig) {
			if(k in subtypes) config.putAll(v)
		}
		return config
	}
}


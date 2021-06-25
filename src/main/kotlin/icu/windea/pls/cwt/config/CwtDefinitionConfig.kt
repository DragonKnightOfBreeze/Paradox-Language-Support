package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import java.util.WeakHashMap

//TODO 这里排序可能出现问题

data class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val config: List<Pair<String?,CwtPropertyConfig>>, //(subtypeExpression, propConfig)
) : CwtConfig<CwtProperty> {
	//使用WeakHashMap - 减少内存占用
	private val mergeConfigCache = WeakHashMap<String,List<CwtPropertyConfig>>()

	fun mergeConfig(subtypes: List<String>): List<CwtPropertyConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergeConfigCache.getOrPut(cacheKey){
			val result = mutableListOf<CwtPropertyConfig>()
			for((subtypeExpression, propConfig) in config) {
				if(subtypeExpression == null || matchesSubtype(subtypeExpression,subtypes)) {
					result.add(propConfig)
				}
			}
			result
		}
	}
	
	private fun matchesSubtype(subtypeExpression: String, subtypes: List<String>): Boolean {
		return if(subtypeExpression.startsWith('!')) subtypeExpression.drop(1) !in subtypes else subtypeExpression in subtypes
	}
}


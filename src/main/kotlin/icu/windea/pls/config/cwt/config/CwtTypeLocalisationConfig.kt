package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

data class CwtTypeLocalisationConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	val configs: List<Pair<String?, CwtTypeLocalisationInfoConfig>> //(subtypeExpression, typeLocalisationInfoConfig)
) : CwtConfig<CwtProperty> {
	private val mergesConfigsCache: Cache<String, List<CwtTypeLocalisationInfoConfig>> by lazy { createCache() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtTypeLocalisationInfoConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergesConfigsCache.getOrPut(cacheKey){
			val result = SmartList<CwtTypeLocalisationInfoConfig>()
			for((subtypeExpression, typeLocalisationInfoConfig) in configs) {
				if(subtypeExpression == null || matchesSubtypeExpression(subtypeExpression, subtypes)) {
					result.add(typeLocalisationInfoConfig)
				}
			}
			result
		}
	}
}
package icu.windea.pls.config.cwt

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import java.util.*

data class CwtTypeLocalisationConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	val configs: List<Pair<String?, CwtTypeLocalisationInfoConfig>> //(subtypeExpression, typeLocalisationInfoConfig)
): CwtConfig<CwtProperty> {
	//使用WeakHashMap - 减少内存占用
	private val mergeConfigsCache = WeakHashMap<String, List<CwtTypeLocalisationInfoConfig>>()
	
	/**
	 * 根据子类型列表合并配置。
	 */
	fun mergeConfigs(subtypes: List<String>): List<CwtTypeLocalisationInfoConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergeConfigsCache.getOrPut(cacheKey){
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
package icu.windea.pls.config.config

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*

class CwtTypeLocalisationConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val configs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)
) : CwtConfig<CwtProperty> {
	private val mergedConfigCache: Cache<String, List<CwtLocationConfig>> = CacheBuilder.newBuilder().buildCache()
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtLocationConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergedConfigCache.getOrPut(cacheKey){
			val result = mutableListOf<CwtLocationConfig>()
			for((subtypeExpression, locationConfig) in configs) {
				if(subtypeExpression == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
					result.add(locationConfig)
				}
			}
			result
		}
	}
}
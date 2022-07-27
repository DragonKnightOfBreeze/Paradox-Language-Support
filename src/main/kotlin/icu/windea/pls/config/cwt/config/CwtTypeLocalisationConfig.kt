package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

data class CwtTypeLocalisationConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigInfo,
	val configs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)
) : CwtConfig<CwtProperty> {
	private val mergesConfigsCache: Cache<String, List<CwtLocationConfig>> by lazy { CacheBuilder.newBuilder().build() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtLocationConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergesConfigsCache.getOrPut(cacheKey){
			val result = SmartList<CwtLocationConfig>()
			for((subtypeExpression, locationConfig) in configs) {
				if(subtypeExpression == null || matchesDefinitionSubtypeExpression(subtypeExpression, subtypes)) {
					result.add(locationConfig)
				}
			}
			result
		}
	}
}
package icu.windea.pls.config.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.cwt.psi.*

class CwtTypeImagesConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val configs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)
) : CwtConfig<CwtProperty> {
	private val mergesConfigCache: Cache<String, List<CwtLocationConfig>> by lazy { CacheBuilder.newBuilder().buildCache() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtLocationConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergesConfigCache.getOrPut(cacheKey){
			val result = SmartList<CwtLocationConfig>()
			for((subtypeExpression, locationConfig) in configs) {
				if(subtypeExpression == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
					result.add(locationConfig)
				}
			}
			result
		}
	}
}
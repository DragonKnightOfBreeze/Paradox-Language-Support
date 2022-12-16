package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtDeclarationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
	private val mergedConfigCache: Cache<String, CwtPropertyConfig> by lazy { CacheBuilder.newBuilder().buildCache() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfig(subtypes: List<String> = emptyList()): CwtPropertyConfig {
		val properties = propertyConfig.properties
		val values = propertyConfig.values
		
		//定义的值不为代码块的情况
		if(properties == null && values == null) return propertyConfig
		
		val cacheKey = subtypes.joinToString(",")
		return mergedConfigCache.getOrPut(cacheKey) {
			propertyConfig.copy(
				configs = propertyConfig.configs?.flatMap { it.deepMergeBySubtypes(subtypes) }
			)
		}
	}
}
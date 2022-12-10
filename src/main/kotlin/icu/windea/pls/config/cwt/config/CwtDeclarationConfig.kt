package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

data class CwtDeclarationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
	val propertyConfigSingletonList by lazy { propertyConfig.toSingletonList() }
	
	private val mergedConfigCache: Cache<String, List<CwtDataConfig<*>>> by lazy { CacheBuilder.newBuilder().buildCache() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtDataConfig<*>> {
		val properties = propertyConfig.properties
		val values = propertyConfig.values
		
		//定义的值不为代码块的情况
		if(properties == null && values == null) return propertyConfigSingletonList
		
		val cacheKey = subtypes.joinToString(",")
		return mergedConfigCache.getOrPut(cacheKey) {
			val mergedConfigs = SmartList<CwtDataConfig<*>>()
			if(properties != null && properties.isNotEmpty()) {
				properties.forEach { mergedConfigs.addAll(it.deepMergeBySubtypes(subtypes)) }
			}
			if(values != null && values.isNotEmpty()) {
				values.forEach { mergedConfigs.addAll(it.deepMergeBySubtypes(subtypes)) }
			}
			mergedConfigs
		}
	}
}
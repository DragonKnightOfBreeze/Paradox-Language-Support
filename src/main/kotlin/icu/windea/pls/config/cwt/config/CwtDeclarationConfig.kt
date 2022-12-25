package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtDeclarationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
	companion object{
		private val mergedConfigCache: Cache<String, CwtPropertyConfig> by lazy { CacheBuilder.newBuilder().buildCache() }
	}
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfig(subtypes: List<String>?, name: String?): CwtPropertyConfig {
		//定义的值不为代码块的情况
		if(!propertyConfig.isBlock) return propertyConfig
		
		val type = this.name
		val configGroup = info.configGroup
		val cacheKey = buildString { 
			if(CwtConfigExpressionHandler.shouldHandle(name, type, subtypes,configGroup)) {
				append(name).append(" ")
			}
			append(type).append(" ")
			if(subtypes != null) {
				append(subtypes.joinToString(","))
			} else {
				append("*")
			}
		}
		return mergedConfigCache.getOrPut(cacheKey) {
			propertyConfig.copy(
				configs = propertyConfig.configs?.flatMap { it.deepMergeConfigs(name, type, subtypes, configGroup) }
			)
		}
	}
}
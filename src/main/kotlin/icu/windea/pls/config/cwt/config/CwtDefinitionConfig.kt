package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
	val configs: List<Pair<String?, CwtPropertyConfig>>? = null, //(subtypeExpression?, propConfig)
) : CwtConfig<CwtProperty> {
	private val mergeConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { createCache() }
	private val configsCache: Cache<String, List<CwtKvConfig<*>>> by lazy { createCache() }
	private val childPropertyConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { createCache() }
	private val childValueConfigsCache: Cache<String, List<CwtValueConfig>> by lazy { createCache() }
	
	private val propertyConfigList by lazy { propertyConfig.toSingletonList() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtPropertyConfig> {
		return when {
			configs == null -> propertyConfigList //定义的值不为代码块的情况
			configs.isEmpty() -> emptyList()
			else -> {
				val cacheKey = subtypes.joinToString(",")
				mergeConfigsCache.getOrPut(cacheKey) {
					val result = SmartList<CwtPropertyConfig>()
					for((subtypeExpression, propConfig) in configs) {
						if(subtypeExpression == null || matchesDefinitionSubtypeExpression(subtypeExpression, subtypes)) {
							result.add(propConfig)
						}
					}
					result
				}
			}
		}
	}
	
	/**
	 * 根据路径解析对应的属性/值配置列表。
	 */
	fun resolveConfigs(subtypes: List<String>, path: ParadoxElementPath<*>, configGroup: CwtConfigGroup): List<CwtKvConfig<*>> {
		//TODO path可以对应property或者value
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return configsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的属性列表即是定义本身组成的单例列表
				path.isEmpty() -> propertyConfigList
				else -> {
					var result: List<CwtKvConfig<*>> = getMergedConfigs(subtypes)
					var isTop = true
					for(key in path.originalSubPaths) {
						val isQuoted = key.isQuoted()
						//如果aliasName是effect或trigger，则key也可以是links中的link，或者其嵌套格式（root.owner），这时需要略过
						
						//如果是顶级的就不要打平，否则要打平，然后还需要根据是否匹配keyExpression进行过滤
						//如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
						if(isTop) {
							isTop = false
							val nextResult = SmartList<CwtKvConfig<*>>()
							for(config in result) {
								when {
									config is CwtPropertyConfig -> {
										if(CwtConfigHandler.matchesKey(config.keyExpression, key, ParadoxValueType.infer(key), isQuoted, configGroup)) {
											CwtConfigHandler.inlinePropertyConfig(key, isQuoted, config, configGroup, nextResult)
										}
									}
									config is CwtValueConfig -> {
										nextResult.add(config)
									}
								}
							}
							result = nextResult
						} else {
							val nextResult = SmartList<CwtKvConfig<*>>()
							for(r in result) {
								//如果任意父路径的aliasName是effect或者trigger，且当前key匹配links中的link，或者其嵌套格式（root.owner），则需要跳过当前key
								if(r is CwtPropertyConfig && r.rawAliasConfig?.name.let { it == "effect" || it == "trigger" }){
									if(CwtConfigHandler.matchesLinkExpression(key, configGroup)) continue
								}
								
								val propertyConfigs = r.properties
								if(propertyConfigs != null && propertyConfigs.isNotEmpty()) {
									for(propertyConfig in propertyConfigs) {
										if(CwtConfigHandler.matchesKey(propertyConfig.keyExpression, key, ParadoxValueType.infer(key), isQuoted, configGroup)) {
											CwtConfigHandler.inlinePropertyConfig(key, isQuoted, propertyConfig, configGroup, nextResult)
										}
									}
								}
								val valueConfigs = r.values
								if(valueConfigs != null && valueConfigs.isNotEmpty()){
									for(valueConfig in valueConfigs){
										nextResult.add(valueConfig)
									}
								}
							}
							result = nextResult
						}
					}
					result
				}
			}
		}
	}
	
	/**
	 * 根据路径解析对应的子属性配置列表。（过滤重复的）
	 */
	fun resolveChildPropertyConfigs(subtypes: List<String>, path: ParadoxElementPath<*>, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		//parentPath可以对应property或者value
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return childPropertyConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
				path.isEmpty() -> getMergedConfigs(subtypes)
				else -> {
					//打平propertyConfigs中的每一个properties
					val propertyConfigs = resolveConfigs(subtypes, path, configGroup)
					val result = SmartList<CwtPropertyConfig>()
					for(propertyConfig in propertyConfigs) {
						val props = propertyConfig.properties
						if(props != null && props.isNotEmpty()) result.addAll(props)
					}
					result
				}
			}
		}.distinctBy { it.key }
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	fun resolveChildValuesConfigs(subtypes: List<String>, path: ParadoxElementPath<*>, configGroup: CwtConfigGroup): List<CwtValueConfig> {
		//parentPath可以对应property或者value
		val cacheKey = "${subtypes.joinToString(",")}$path"
		return childValueConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的就是顶级值列表（定义的代码块类型的值中的值列表）
				path.isEmpty() -> propertyConfig.values ?: emptyList()
				else -> {
					//打平propertyConfigs中的每一个values
					val propertyConfigs = resolveConfigs(subtypes, path, configGroup)
					val result = SmartList<CwtValueConfig>()
					for(propertyConfig in propertyConfigs) {
						val values = propertyConfig.values
						if(values != null && values.isNotEmpty()) result.addAll(values)
					}
					result
				}
			}
		}.distinctBy { it.value }
	}
}


package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

data class CwtDeclarationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
	val configs: List<Pair<String?, CwtPropertyConfig>>? = null, //(subtypeExpression?, propConfig)
) : CwtConfig<CwtProperty> {
	private val mergeConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { CacheBuilder.newBuilder().build() }
	private val configsCache: Cache<String, List<CwtKvConfig<*>>> by lazy { CacheBuilder.newBuilder().build() }
	private val childPropertyConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { CacheBuilder.newBuilder().build() }
	private val childValueConfigsCache: Cache<String, List<CwtValueConfig>> by lazy { CacheBuilder.newBuilder().build() }
	
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
		//如果路径中可能待遇参数，则不进行解析
		if(path.isParameterAware) return emptyList()
		
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return configsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的属性列表即是定义本身组成的单例列表
				path.isEmpty() -> propertyConfigList
				else -> {
					var result: List<CwtKvConfig<*>> = getMergedConfigs(subtypes)
					var index = 0
					while(index < path.length) {
						val originalKey = path.originalSubPaths[index]
						val key = originalKey.unquote()
						val isQuoted = key.isQuoted()
						var nextIndex = index + 1
						
						//如果aliasName是effect或trigger，则key也可以是links中的link，或者其嵌套格式（root.owner），这时需要跳过当前的key
						//如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
						
						val nextResult = SmartList<CwtKvConfig<*>>()
						for(config in result) {
							if(index == 0) {
								when {
									config is CwtPropertyConfig -> {
										if(CwtConfigHandler.matchesKey(config.keyExpression, key, ParadoxValueType.infer(key), isQuoted, configGroup)) {
											nextIndex = inlineConfig(key, isQuoted, config, configGroup, nextResult, index, path)
										}
									}
									config is CwtValueConfig -> {
										nextResult.add(config)
									}
								}
							} else {
								val propertyConfigs = config.properties
								if(propertyConfigs != null && propertyConfigs.isNotEmpty()) {
									for(propertyConfig in propertyConfigs) {
										if(CwtConfigHandler.matchesKey(propertyConfig.keyExpression, key, ParadoxValueType.infer(key), isQuoted, configGroup)) {
											nextIndex = inlineConfig(key, isQuoted, propertyConfig, configGroup, nextResult, index, path)
										}
									}
								}
								val valueConfigs = config.values
								if(valueConfigs != null && valueConfigs.isNotEmpty()) {
									for(valueConfig in valueConfigs) {
										nextResult.add(valueConfig)
									}
								}
							}
						}
						result = nextResult
						index = nextIndex
					}
					//需要按优先级重新排序
					if(result is MutableList) result.sortByDescending { it.expression.priority }
					result
				}
			}.sortedByDescending { it.expression.priority } //需要按照优先级重新排序
		}
	}
	
	/**
	 * 内联规则以便后续的代码提示、引用解析和结构验证。
	 */
	@Suppress("NAME_SHADOWING")
	private fun inlineConfig(key: String, isQuoted:Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup, result: MutableList<CwtKvConfig<*>>, index: Int, path: ParadoxElementPath<*>): Int {
		//内联类型为`single_alias_right`或`alias_match_left`的规则
		//直到不是linkExpression（匹配links中的link，或者其嵌套格式（root.owner））为止，跳过下一个key
		//如果这里得到的配置有对应的singleAliasConfig/aliasConfig且支持linkExpression
		//且当前key匹配links中的link，或者其嵌套格式（root.owner），则需要跳过当前key
		var key = key
		var isQuoted = isQuoted
		var index = index
		run {
			var inlinedScopes: MutableList<String>? = null
			if(CwtConfigHandler.supportsScopes(config)) {
				while(index < path.length) {
					val originalKey = path.originalSubPaths[index]
					key = originalKey.unquote()
					isQuoted = originalKey.isQuoted()
					if(isQuoted || !CwtConfigHandler.matchesLinkExpression(key, configGroup)) break
					if(inlinedScopes == null) inlinedScopes = SmartList()
					inlinedScopes.addAll(key.split('.'))
					index++
				}
			}
			val valueExpression = config.valueExpression
			when(valueExpression.type) {
				CwtDataTypes.SingleAliasRight -> {
					val singleAliasName = valueExpression.value ?: return@run
					val singleAliases = configGroup.singleAliases[singleAliasName] ?: return@run
					for(singleAlias in singleAliases) {
						result.add(config.inlineFromSingleAliasConfig(singleAlias, inlinedScopes))
					}
					return index + 1
				}
				CwtDataTypes.AliasMatchLeft -> {
					val aliasName = valueExpression.value ?: return@run
					val aliasGroup = configGroup.aliases[aliasName] ?: return@run
					val aliasSubName = CwtConfigHandler.resolveAliasSubNameExpression(key, isQuoted, aliasGroup, configGroup) ?: return@run
					val aliases = aliasGroup[aliasSubName] ?: return@run
					for(alias in aliases) {
						result.add(config.inlineFromAliasConfig(alias, inlinedScopes))
					}
					return index + 1
				}
				else -> pass()
			}
		}
		result.add(config)
		return index + 1
	}
	
	/**
	 * 根据路径解析对应的子属性配置列表。（过滤重复的）
	 */
	fun resolveChildPropertyConfigs(subtypes: List<String>, path: ParadoxElementPath<*>, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		//如果路径中可能待遇参数，则不进行解析
		if(path.isParameterAware) return emptyList()
		
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
			}.distinctBy { it.key }.sortedByDescending { it.expression.priority } //需要按照优先级重新排序
		}
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	fun resolveChildValuesConfigs(subtypes: List<String>, path: ParadoxElementPath<*>, configGroup: CwtConfigGroup): List<CwtValueConfig> {
		//如果路径中可能待遇参数，则不进行解析
		if(path.isParameterAware) return emptyList()
		
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
			}.distinctBy { it.value }.sortedByDescending { it.expression.priority } //需要按照优先级重新排序
		}
	}
}


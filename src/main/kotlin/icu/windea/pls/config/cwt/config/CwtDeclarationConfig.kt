package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.matchesScriptExpression
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.model.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.expression.*

data class CwtDeclarationConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
	private val mergeConfigsCache: Cache<String, List<CwtDataConfig<*>>> by lazy { CacheBuilder.newBuilder().buildCache() }
	private val configsCache: Cache<String, List<CwtDataConfig<*>>> by lazy { CacheBuilder.newBuilder().buildCache() }
	private val childPropertyConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { CacheBuilder.newBuilder().buildCache() }
	private val childValueConfigsCache: Cache<String, List<CwtValueConfig>> by lazy { CacheBuilder.newBuilder().buildCache() }
	
	private val propertyConfigList by lazy { propertyConfig.toSingletonList() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergedConfigs(subtypes: List<String>): List<CwtDataConfig<*>> {
		val properties = propertyConfig.properties
		val values = propertyConfig.values
		
		//定义的值不为代码块的情况
		if(properties == null && values == null) return propertyConfigList
		
		val cacheKey = subtypes.joinToString(",")
		return mergeConfigsCache.getOrPut(cacheKey) {
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
	
	/**
	 * 根据路径解析对应的属性/值配置列表。
	 */
	fun resolveConfigs(subtypes: List<String>, path: ParadoxElementPath, configGroup: CwtConfigGroup): List<CwtDataConfig<*>> {
		//如果路径中可能待遇参数，则不进行解析
		if(path.isParameterAware) return emptyList()
		
		//FIXME 需要重新调整对返回的规则列表的排序
		//这里可能出现ProcessCanceledException，这时直接返回空列表
		
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return configsCache.getOrPut(cacheKey, { emptyList() }) {
			doResolveConfigs(subtypes, path, configGroup)
		}
	}
	
	private fun doResolveConfigs(subtypes: List<String>, path: ParadoxElementPath, configGroup: CwtConfigGroup) =
		when {
			//这里的属性路径可以为空，这时得到的属性列表即是定义本身组成的单例列表
			path.isEmpty() -> propertyConfigList
			else -> {
				var result: List<CwtDataConfig<*>> = getMergedConfigs(subtypes)
				var index = 0
				while(index < path.length) {
					val (key, isQuoted, isKey) = path.subPathInfos[index]
					var nextIndex = index + 1
					
					//如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
					
					val expression = ParadoxScriptExpression.resolve(key, isQuoted, true)
					val nextResult = SmartList<CwtDataConfig<*>>()
					for(config in result) {
						if(index == 0) {
							if(isKey && config is CwtPropertyConfig) {
								if(matchesScriptExpression(expression, config.keyExpression, configGroup)) {
									nextIndex = inlineConfig(key, isQuoted, config, configGroup, nextResult, index, path)
								}
							} else if(!isKey && config is CwtValueConfig) {
								nextResult.add(config)
							}
						} else {
							val propertyConfigs = config.properties
							if(isKey && propertyConfigs != null && propertyConfigs.isNotEmpty()) {
								for(propertyConfig in propertyConfigs) {
									if(matchesScriptExpression(expression, propertyConfig.keyExpression, configGroup)) {
										nextIndex = inlineConfig(key, isQuoted, propertyConfig, configGroup, nextResult, index, path)
									}
								}
							}
							val valueConfigs = config.values
							if(!isKey && valueConfigs != null && valueConfigs.isNotEmpty()) {
								for(valueConfig in valueConfigs) {
									nextResult.add(valueConfig)
								}
							}
						}
					}
					
					//如果存在可以静态匹配（CwtConfigMatchType.STATIC）的规则，则仅选用可以静态匹配的规则
					result = nextResult.filter { 
						matchesScriptExpression(expression, it.expression, configGroup, CwtConfigMatchType.STATIC)
					}.ifEmpty { nextResult }
					index = nextIndex
				}
				result.sortedByPriority(configGroup) { it.expression }
			}
		}
	
	/**
	 * 内联规则以便后续的代码提示、引用解析和结构验证。
	 */
	private fun inlineConfig(key: String, isQuoted: Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup, result: MutableList<CwtDataConfig<*>>, index: Int, path: ParadoxElementPath): Int {
		//内联类型为`single_alias_right`或`alias_match_left`的规则
		run {
			val valueExpression = config.valueExpression
			when(valueExpression.type) {
				CwtDataTypes.SingleAliasRight -> {
					val singleAliasName = valueExpression.value ?: return@run
					val singleAliases = configGroup.singleAliases[singleAliasName] ?: return@run
					for(singleAlias in singleAliases) {
						result.add(config.inlineFromSingleAliasConfig(singleAlias))
					}
					return index + 1
				}
				CwtDataTypes.AliasMatchLeft -> {
					val aliasName = valueExpression.value ?: return@run
					val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
					val aliasSubName = CwtConfigHandler.getAliasSubName(key, isQuoted, aliasName, configGroup) ?: return@run
					val aliases = aliasGroup[aliasSubName] ?: return@run
					for(alias in aliases) {
						result.add(config.inlineFromAliasConfig(alias))
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
	fun resolveChildPropertyConfigs(subtypes: List<String>, path: ParadoxElementPath, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		//如果路径中可能待遇参数，则不进行解析
		if(path.isParameterAware) return emptyList()
		
		//parentPath可以对应property或者value
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return childPropertyConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
				path.isEmpty() -> getMergedConfigs(subtypes).filterIsInstance<CwtPropertyConfig>()
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
		}
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	fun resolveChildValuesConfigs(subtypes: List<String>, path: ParadoxElementPath, configGroup: CwtConfigGroup): List<CwtValueConfig> {
		//如果路径中可能待遇参数，则不进行解析
		if(path.isParameterAware) return emptyList()
		
		//parentPath可以对应property或者value
		val cacheKey = "${subtypes.joinToString(",")}$path"
		return childValueConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的就是顶级值列表（定义的代码块类型的值中的值列表）
				path.isEmpty() -> getMergedConfigs(subtypes).filterIsInstance<CwtValueConfig>()
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
		}
	}
}


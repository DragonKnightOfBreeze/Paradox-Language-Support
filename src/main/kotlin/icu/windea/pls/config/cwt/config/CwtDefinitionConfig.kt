package icu.windea.pls.config.cwt.config

import com.google.common.cache.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val propertyConfig: CwtPropertyConfig, //definitionName = ...
	val configs: List<Pair<String?, CwtPropertyConfig>>? = null, //(subtypeExpression?, propConfig)
) : CwtConfig<CwtProperty> {
	private val mergeConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { createCache() }
	private val propertyConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { createCache() }
	private val childPropertyConfigsCache: Cache<String, List<CwtPropertyConfig>> by lazy { createCache() }
	private val childValueConfigsCache: Cache<String, List<CwtValueConfig>> by lazy { createCache() }
	
	private val propertyConfigList by lazy { propertyConfig.toSingletonList() }
	
	/**
	 * 得到根据子类型列表进行合并后的配置。
	 */
	fun getMergeConfigs(subtypes: List<String>): List<CwtPropertyConfig> {
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
	 * 根据路径解析对应的属性配置列表。
	 */
	fun resolvePropertyConfigs(subtypes: List<String>, path: ParadoxElementPath<*>, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return propertyConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的属性列表即是定义本身组成的单例列表
				path.isEmpty() -> propertyConfigList
				else -> {
					var result = getMergeConfigs(subtypes)
					var isTop = true
					for(key in path.originalSubPaths) {
						val isQuoted = key.isQuoted()
						//如果是顶级的就不要打平，否则要打平，然后还需要根据是否匹配keyExpression进行过滤
						//如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left
						//则需要内联这些规则
						if(isTop) {
							isTop = false
							val nextResult = SmartList<CwtPropertyConfig>()
							for(config in result) {
								if(matchesKey(config.keyExpression, key, isQuoted, configGroup)) {
									//如果valueExpressionType是single_alias_right或alias_match_left,则要进行内联
									val inlined = inlineConfig(key, isQuoted, config, configGroup, nextResult)
									if(!inlined) nextResult.add(config)
								}
							}
							result = nextResult
						} else {
							val nextResult = SmartList<CwtPropertyConfig>()
							for(r in result) {
								val configs = r.properties
								if(configs != null && configs.isNotEmpty()) {
									for(config in configs) {
										if(matchesKey(config.keyExpression, key, isQuoted, configGroup)) {
											//如果valueExpressionType是single_alias_right或alias_match_left,则要进行内联
											val inlined = inlineConfig(key, isQuoted, config, configGroup, nextResult)
											if(!inlined) nextResult.add(config)
										}
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
		val cacheKey = "${subtypes.joinToString(",")}:$path"
		return childPropertyConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的就是顶级属性列表
				path.isEmpty() -> getMergeConfigs(subtypes)
				else -> {
					//打平propertyConfigs中的每一个properties
					val propertyConfigs = resolvePropertyConfigs(subtypes, path, configGroup)
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
		val cacheKey = "${subtypes.joinToString(",")}$path"
		return childValueConfigsCache.getOrPut(cacheKey) {
			when {
				//这里的属性路径可以为空，这时得到的是空列表（如果定义的值的类型是代码块，代码块中的应当是属性列表）
				path.isEmpty() -> emptyList()
				else -> {
					//打平propertyConfigs中的每一个values
					val propertyConfigs = resolvePropertyConfigs(subtypes, path, configGroup)
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
	
	/**
	 * 内联类型为`single_alias_right`或`alias_match_left`的规则。以便后续的代码提示、引用解析和结构验证。
	 */
	private fun inlineConfig(key: String, quoted: Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup,
		result: MutableList<CwtPropertyConfig>): Boolean {
		val valueExpression = config.valueExpression
		return when(valueExpression.type) {
			CwtValueExpression.Type.SingleAliasRight -> {
				val singleAliasName = valueExpression.value ?: return false
				val singleAliases = configGroup.singleAliases[singleAliasName] ?: return false
				for(singleAlias in singleAliases) {
					val c = singleAlias.config.copy(
						pointer = config.pointer, key = config.key,
						options = config.options, optionValues = config.optionValues, documentation = config.documentation
					)
					c.parent = config.parent
					result.add(c)
				}
				true
			}
			CwtValueExpression.Type.AliasMatchLeft -> {
				val aliasName = valueExpression.value ?: return false
				val aliasGroup = configGroup.aliases[aliasName] ?: return false
				val aliasSubName = resolveAliasSubNameExpression(key, quoted, aliasGroup, configGroup) ?: return false
				val aliases = aliasGroup[aliasSubName] ?: return false
				for(alias in aliases) {
					val c = alias.config.copy(
						pointer = config.pointer, key = config.key,
						options = config.options, optionValues = config.optionValues, documentation = config.documentation
					)
					c.parent = config.parent
					result.add(c)
				}
				true
			}
			else -> false
		}
	}
}


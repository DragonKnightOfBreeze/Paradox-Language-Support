package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.model.*
import com.windea.plugin.idea.pls.script.psi.*
import org.slf4j.*
import java.util.concurrent.*

/*
TODO
 * flat config
   * types - types
   * enums - enums
   * definitions - *
   * alias - alias
   * root declarations - * (in root directory)
 */

class CwtConfigGroupCache(
	val group: Map<String, CwtConfig>,
	val gameType: ParadoxGameType,
	val name: String
) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
		
		private const val typesKey = "types"
		private const val typeKeyPrefix = "type["
		private const val typeKeySuffix = "]"
		private const val subtypeKeyPrefix = "subtype["
		private const val subtypeKeySuffix = "]"
		private const val enumsKey = "enums"
		private const val enumKeyPrefix = "enum["
		private const val enumKeySuffix = "]"
		private const val aliasKeyPrefix = "alias["
		private const val aliasKeySuffix = "]"
		private const val modifierPrefix = "modifier:"
		private const val effectPrefix = "effect:"
		private const val scopesKey = "scopes"
		private const val scopeGroupsKey = "scope_groups"
	}
	
	//? -> type[?] = { ... }
	val types: Map<String, CwtTypeConfig>
	
	//? -> [a, b, 1, 2, yes]
	//枚举值也有可能是int、number、bool类型，这里统一用字符串表示
	val enums: Map<String, List<String>>
	
	//? -> alias[?] = ...
	//? -> alias[?] = { ... }
	val aliases: Map<String, CwtConfigProperty>
	
	init {
		logger.info("Resolve config group '$name'...")
		
		types = ConcurrentHashMap()
		enums = ConcurrentHashMap()
		aliases = ConcurrentHashMap()
		
		for((_, config) in group) {
			for(prop in config.properties) {
				val key = prop.key
				when(key) {
					//找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
					typesKey -> {
						val typeProperties = prop.properties
						if(typeProperties != null && typeProperties.isNotEmpty()) {
							for(typeProperty in typeProperties) {
								val typeName = resolveTypeName(typeProperty.key)
								if(typeName != null && typeName.isNotEmpty()) {
									types[typeName] = resolveTypeConfig(typeProperty, typeName) ?: continue
								}
							}
						}
						continue
					}
					//找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums中
					enumsKey -> {
						val enumProperties = prop.properties
						if(enumProperties != null && enumProperties.isNotEmpty()) {
							for(enumProperty in enumProperties) {
								val enumName = resolveEnumName(enumProperty.key)
								if(enumName != null && enumName.isNotEmpty()) {
									enums[enumName] = resolveEnumConfig(enumProperty) ?: continue
								}
							}
						}
						continue
					}
				}
				//判断配置文件中的顶级的key是否匹配"alias[?]"，如果匹配，则解析它的子属性（或它的值），添加到aliases中
				val aliasName = resolveAliasName(key)
				if(aliasName != null) {
					if(aliasName.isEmpty()) continue //忽略aliasName为空字符串的情况
					val aliasProperty = prop
					aliases[aliasName] = aliasProperty
				}
			}
		}
		
		logger.info("Resolve config group '$name' finished.")
	}
	
	private fun resolveTypeName(key: String): String? {
		return key.resolveByRemoveSurrounding(typeKeyPrefix, typeKeySuffix)
	}
	
	private fun resolveSubtypeName(key: String): String? {
		return key.resolveByRemoveSurrounding(subtypeKeyPrefix, subtypeKeySuffix)
	}
	
	private fun resolveEnumName(key: String): String? {
		return key.resolveByRemoveSurrounding(enumKeyPrefix, enumKeySuffix)
	}
	
	private fun resolveAliasName(key: String): String? {
		return key.resolveByRemoveSurrounding(aliasKeyPrefix, aliasKeySuffix)
	}
	
	private fun resolveTypeConfig(config: CwtConfigProperty, name: String): CwtTypeConfig? {
		val props = config.properties ?: return null
		val resolved = CwtTypeConfig(name)
		
		if(props.isNotEmpty()) {
			for(prop in props) {
				val key = prop.key
				when(key) {
					"path" -> resolved.path = prop.stringValue ?: continue
					"path_strict" -> resolved.path_strict = prop.booleanValue ?: continue
					"path_file" -> resolved.path_file = prop.stringValue ?: continue
					"path_extension" -> resolved.path_extension = prop.stringValue ?: continue
					"name_field" -> resolved.name_field = prop.stringValue ?: continue
					"name_from_file" -> resolved.name_from_file = prop.booleanValue ?: continue
					"type_per_file" -> resolved.type_per_file = prop.booleanValue ?: continue
					"unique" -> resolved.unique = prop.booleanValue ?: continue
					"severity" -> resolved.severity = prop.stringValue ?: continue
					"skip_root_key" -> {
						val propValue = prop.stringValue
						if(propValue != null) {
							resolved.skip_root_key.add(listOf(propValue))
						} else {
							val propValues = prop.values
							if(propValues != null) {
								resolved.skip_root_key.add(propValues.mapNotNull { it.stringValue })
							}
						}
					}
					"localisation" -> {
						val propProps = prop.properties
						if(propProps != null) {
							for(p in propProps) {
								val k = p.key
								val subtypeName = resolveSubtypeName(k)
								if(subtypeName != null) {
									val pps = p.properties ?: continue
									val localisationConfigs = mutableListOf<CwtTypeLocalisationConfig>()
									for(pp in pps) {
										val kk = pp.key
										val localisationConfig = resolveTypeLocalisationConfig(pp, kk) ?: continue
										localisationConfigs.add(localisationConfig)
									}
									resolved.localisation.put(subtypeName, localisationConfigs)
								} else {
									val localisationConfigs = resolved.localisation.getOrPut("") { mutableListOf() }
									val localisationConfig = resolveTypeLocalisationConfig(p, k) ?: continue
									localisationConfigs.add(localisationConfig)
								}
							}
						}
					}
				}
				
				val subtypeName = resolveSubtypeName(key)
				if(subtypeName != null) {
					resolved.subtypes[subtypeName] = resolveSubtypeConfig(prop, subtypeName)
				}
			}
		}
		
		val options = config.options
		if(options != null && options.isNotEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"starts_with" -> resolved.starts_with = option.stringValue ?: continue
					"type_key_filter" -> {
						val reversed = option.separator == CwtConfigSeparator.NOT_EQUAL
						val optionValues = option.values ?: continue
						val list = optionValues.mapNotNull { it.stringValue }
						resolved.type_key_filter = ReversibleList(list, reversed)
					}
					"graph_related_types" -> {
						val optionValues = option.values ?: continue
						val list = optionValues.mapNotNull { it.stringValue }
						resolved.graph_related_types = list
					}
				}
			}
		}
		
		return resolved
	}
	
	private fun resolveSubtypeConfig(config: CwtConfigProperty, name: String): CwtSubtypeConfig {
		val resolved = CwtSubtypeConfig(name, config)
		
		val options = config.options
		if(options != null && options.isNotEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"type_key_filter" -> {
						val reversed = option.separator == CwtConfigSeparator.NOT_EQUAL
						val optionValues = option.values ?: continue
						val list = optionValues.mapNotNull { it.stringValue }
						resolved.type_key_filter = ReversibleList(list, reversed)
					}
					"push_scope" -> resolved.push_scope = option.stringValue ?: continue
					"starts_with" -> resolved.starts_with = option.stringValue ?: continue
					"display_name" -> resolved.display_name = option.stringValue ?: continue
					"abbreviation" -> resolved.abbreviation = option.stringValue ?: continue
				}
			}
		}
		
		return resolved
	}
	
	private fun resolveTypeLocalisationConfig(config: CwtConfigProperty, name: String): CwtTypeLocalisationConfig? {
		val expression = config.stringValue ?: return null
		val resolved = CwtTypeLocalisationConfig(name, expression)
		
		val optionValues = config.optionValues
		if(optionValues != null && optionValues.isNotEmpty()) {
			for(optionValue in optionValues) {
				val value = optionValue.stringValue ?: continue
				when(value) {
					"required" -> resolved.required = true
					"primary" -> resolved.primary = true
				}
			}
		}
		
		return resolved
	}
	
	private fun resolveEnumConfig(config: CwtConfigProperty): List<String>? {
		val enumConfigValues = config.values ?: return null
		return enumConfigValues.mapNotNull { it.stringValue }
	}
	
	/**
	 * 根据指定的scriptProperty，匹配类型规则，得到对应的definitionInfo。
	 */
	fun getDefinitionInfo(element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPath): ParadoxDefinitionInfo? {
		for((typeName, typeConfig) in types) {
			if(matchesType(typeConfig, element, elementName, path, propertyPath)) {
				
			}
		}
		return null
	}
	
	private fun matchesType(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPath): Boolean {
		return false
	}
}


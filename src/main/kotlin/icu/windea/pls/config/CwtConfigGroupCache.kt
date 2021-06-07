package icu.windea.pls.config

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import org.slf4j.*
import java.util.concurrent.*

class CwtConfigGroupCache(
	val group: Map<String, CwtConfig>,
	val gameType: ParadoxGameType,
	val name: String,
	val project: Project
) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
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
					"types" -> {
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
					"enums" -> {
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
	
	private fun resolveTypeConfig(config: CwtConfigProperty, name: String): CwtTypeConfig? {
		val props = config.properties ?: return null
		val resolved = CwtTypeConfig(name)
		
		if(props.isNotEmpty()) {
			for(prop in props) {
				val key = prop.key
				when(key) {
					//这里path需要移除前缀"game/"，这个插件会忽略它
					"path" -> resolved.path = prop.stringValue?.removePrefix("game/") ?: continue
					"path_strict" -> resolved.path_strict = prop.booleanValue ?: continue
					"path_file" -> resolved.path_file = prop.stringValue ?: continue
					"path_extension" -> resolved.path_extension = prop.stringValue ?: continue
					"name_field" -> resolved.name_field = prop.stringValue ?: continue
					"name_from_file" -> resolved.name_from_file = prop.booleanValue ?: continue
					"type_per_file" -> resolved.type_per_file = prop.booleanValue ?: continue
					"unique" -> resolved.unique = prop.booleanValue ?: continue
					"severity" -> resolved.severity = prop.stringValue ?: continue
					"skip_root_key" -> {
						//值可能是string也可能是stringArray
						val list = prop.stringValue?.toSingletonList() ?: prop.values?.mapNotNull { it.stringValue }?: emptyList()
						if(list.isNotEmpty()) resolved.skip_root_key.add(list)
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
						//值可能是string也可能是stringArray
						val list = option.stringValue?.toSingletonList() ?: option.values?.mapNotNull { it.stringValue } ?: emptyList()
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
						//值可能是string也可能是stringArray
						val list = option.stringValue?.toSingletonList() ?: option.values?.mapNotNull { it.stringValue } ?: emptyList()
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
	fun inferDefinitionInfo(element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPath): ParadoxDefinitionInfo? {
		for(typeConfig in types.values) {
			if(matchesType(typeConfig, element, elementName, path, propertyPath)) {
				return toDefinitionInfo(typeConfig, element, elementName)
			}
		}
		return null
	}
	
	/**
	 * 判断
	 */
	private fun matchesType(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPath): Boolean {
		//判断path是否匹配
		val pathConfig = typeConfig.path ?: return false
		val pathStrictConfig = typeConfig.path_strict
		if(pathStrictConfig) {
			if(pathConfig != path.parent) return false
		} else {
			if(!pathConfig.matchesPath(path.parent)) return false
		}
		//判断path_name是否匹配
		val pathFileConfig = typeConfig.path_file //String?
		if(pathFileConfig != null) {
			if(pathFileConfig != path.fileName) return false
		}
		//判断path_extension是否匹配
		val pathExtensionConfig = typeConfig.path_extension //String?
		if(pathExtensionConfig != null) {
			if(pathExtensionConfig != path.fileExtension) return false
		}
		//如果skip_root_key = <any>，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过
		//skip_root_key可以为列表（多级），可以重复（其中之一匹配即可）
		val skipRootKeyConfig = typeConfig.skip_root_key //String? | "any"
		if(skipRootKeyConfig.isEmpty()) {
			if(propertyPath.length != 1) return false
		}else{
			var skipResult = false
			for(keys in skipRootKeyConfig) {
				if(keys.relaxMatchesPath(propertyPath.parentSubpaths)) {
					skipResult = true
					break
				}
			}
			if(!skipResult) return false
		}
		//如果type_key_filter存在，则过滤key
		val typeKeyFilterConfig = typeConfig.type_key_filter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = if(typeKeyFilterConfig.reverse) {
				typeKeyFilterConfig.all { elementName !in it }
			} else {
				typeKeyFilterConfig.any { elementName in it }
			}
			if(!filterResult) return false
		}
		return true
	}
	
	private fun matchesSubtype(subtypeConfig: CwtSubtypeConfig, element: ParadoxScriptProperty, elementName: String): Boolean {
		//如果type_key_filter存在，则过滤key
		val typeKeyFilterConfig = subtypeConfig.type_key_filter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = if(typeKeyFilterConfig.reverse) {
				typeKeyFilterConfig.all { elementName !in it }
			} else {
				typeKeyFilterConfig.any { elementName in it }
			}
			if(!filterResult) return false
		}
		//如果starts_with存在，则要求elementName匹配这个前缀
		val startsWithConfig = subtypeConfig.starts_with
		if(startsWithConfig != null && startsWithConfig.isNotEmpty()) {
			if(!elementName.startsWith(startsWithConfig)) return false
		}
		//根据config对property进行内容匹配
		val elementConfig = subtypeConfig.config
		return matchContent(element, elementConfig,this)
	}
	
	private fun toDefinitionInfo(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String): ParadoxDefinitionInfo {
		val name = getName(typeConfig, element, elementName)
		val typeKey = elementName
		val type = typeConfig.name
		val subtypesConfig = getSubtypesConfig(typeConfig, element, elementName)
		val subtypes = subtypesConfig.map { it.name }
		val localisationConfig = getLocalisationConfig(typeConfig, subtypes)
		val localisation = getLocalisation(localisationConfig, name)
		val graphRelatedTypes = typeConfig.graph_related_types ?: emptyList()
		val unique = typeConfig.unique
		val severity = typeConfig.severity
		val pushScopes = subtypesConfig.map { it.push_scope }
		return ParadoxDefinitionInfo(name,typeKey, type, subtypes, subtypesConfig, localisation, localisationConfig, 
			graphRelatedTypes, unique, severity, pushScopes)
	}
	
	private fun getName(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String): String {
		//如果name_from_file = yes，则返回文件名（不包含扩展）
		val nameFromFileConfig = typeConfig.name_from_file
		if(nameFromFileConfig) return element.containingFile.name.substringBeforeLast('.')
		//如果name_field = <any>，则返回对应名字的property的value
		val nameFieldConfig = typeConfig.name_field
		if(nameFieldConfig != null) return element.findProperty(nameFieldConfig,true)?.value.orEmpty()
		//如果有一个子属性的propertyKey为name，那么取这个子属性的值，这是为了兼容cwt规则文件尚未考虑到的一些需要名字的情况
		val nameProperty = element.findProperty("name",true)
		if(nameProperty != null) return nameProperty.value.orEmpty()
		//否则直接返回elementName
		return elementName
	}
	
	private fun getSubtypesConfig(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String): List<CwtSubtypeConfig> {
		val subtypesConfig = typeConfig.subtypes
		val result = mutableListOf<CwtSubtypeConfig>()
		for(subtypeConfig in subtypesConfig.values) {
			if(matchesSubtype(subtypeConfig, element, elementName)) result.add(subtypeConfig)
		}
		return result
	}
	
	private fun getSubtypes(subtypesConfig: List<CwtSubtypeConfig>): List<String> {
		return subtypesConfig.map { it.name }
	}
	
	private fun getLocalisationConfig(typeConfig: CwtTypeConfig, subtypes: List<String>): List<CwtTypeLocalisationConfig> {
		val localisationConfig = typeConfig.localisation
		val result = mutableListOf<CwtTypeLocalisationConfig>()
		for((subtypeNameOrEmpty, config) in localisationConfig) {
			if(subtypeNameOrEmpty.isEmpty() || subtypeNameOrEmpty in subtypes) result.addAll(config)
		}
		return result
	}
	
	private fun getLocalisation(localisationConfig: List<CwtTypeLocalisationConfig>, name: String): MutableList<ParadoxLocalisationInfo> {
		val result = mutableListOf<ParadoxLocalisationInfo>()
		for(config in localisationConfig) {
			//如果definition的name是匿名的（没有），那么对应的keyName全部设为匿名的
			val keyName = when(name) {
				anonymousString -> anonymousString
				else -> buildString { for(c in config.expression) if(c == '$') append(name) else append(c) }
			}
			val info = ParadoxLocalisationInfo(config.name, keyName, config.required, config.primary)
			result.add(info)
		}
		return result
	}
}


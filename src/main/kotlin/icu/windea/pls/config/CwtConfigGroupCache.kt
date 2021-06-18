package icu.windea.pls.config

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import org.slf4j.*

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
	
	//? -> { a b 1 2 yes ... }
	//枚举值也有可能是int、number、bool类型，这里统一用字符串表示
	val enums: Map<String, List<String>>
	
	//? -> ? = { ... }
	val links: Map<String, CwtLinkConfig>
	
	//? -> ? = { country planet ... }
	val localisationCommands: Map<String, List<String>>
	
	//? -> ? = { ... }
	val localisationLinks: Map<String, CwtLinkConfig>
	
	//? -> ? = { ... }
	val modifierCategories: Map<String, CwtModifyCategoryConfig>
	
	//? -> ? = { ... }
	val scopes: Map<String, CwtScopeConfig>
	
	//? -> ? = { country planet ... }
	val scopeGroups: Map<String, List<String>>
	
	//? -> alias[?] = ...
	//? -> alias[?] = { ... }
	val aliases: Map<String, CwtConfigProperty>
	
	//? -> ? = { ... subtype[...] = { ... } ... }
	val definitions: Map<String, CwtDefinitionConfig>
	
	init {
		logger.info("Resolve config group '$name'...")
		
		types = mutableMapOf()
		enums = mutableMapOf()
		links = mutableMapOf()
		localisationCommands = mutableMapOf()
		localisationLinks = mutableMapOf()
		modifierCategories = mutableMapOf()
		scopes = mutableMapOf()
		scopeGroups = mutableMapOf()
		aliases = mutableMapOf()
		definitions = mutableMapOf()
		
		for((_, rootProperty) in group) {
			for(property in rootProperty.properties) {
				val key = property.key
				when(key) {
					//找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
					"types" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val typeName = resolveTypeName(prop.key)
								if(typeName != null && typeName.isNotEmpty()) {
									val typeConfig = resolveTypeConfig(prop, typeName)
									if(typeConfig != null) {
										types[typeName] = typeConfig
									}
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums中
					"enums" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val enumName = resolveEnumName(prop.key)
								if(enumName != null && enumName.isNotEmpty()) {
									val enumConfig = resolveEnumConfig(prop)
									if(enumConfig != null) {
										enums[enumName] = enumConfig
									}
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"links"的属性，然后解析它的子属性，添加到links中
					"links" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val linkName = prop.key
								val linkConfig = resolveLinkConfig(prop)
								if(linkConfig != null) {
									links[linkName] = linkConfig
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"localisation_commands"的属性，然后解析它的子属性，添加到localisationCommands中
					"localisation_commands" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val commandName = prop.key
								val commandConfig = resolveLocalisationCommandConfig(prop)
								if(commandConfig != null) {
									localisationCommands[commandName] = commandConfig
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"localisation_links"的属性，然后解析它的子属性，添加到localisationLinks中
					"localisation_links" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val linkName = prop.key
								val linkConfig = resolveLocalisationLinkConfig(prop)
								if(linkConfig != null) {
									localisationLinks[linkName] = linkConfig
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"modifier_categories"的属性，然后解析它的子属性，添加到modifierCategories中
					"modifier_categories" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val categoryName = prop.key
								val categoryConfig = resolveModifierCategoryConfig(prop)
								if(categoryConfig != null) {
									modifierCategories[categoryName] = categoryConfig
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"scopes"的属性，然后解析它的子属性，添加到scopes中
					"scopes" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val scopeName = prop.key
								val scopeConfig = resolveScopeConfig(prop)
								if(scopeConfig != null) {
									scopes[scopeName] = scopeConfig
								}
							}
						}
					}
					//找到配置文件中的顶级的key为"scope_groups"的属性，然后解析它的子属性，添加到scopeGroups中
					"scope_groups" -> {
						val props = property.properties
						if(props != null && props.isNotEmpty()) {
							for(prop in props) {
								val scopeGroupName = prop.key
								val scopeGroupConfig = resolveScopeGroupConfig(prop)
								if(scopeGroupConfig != null) {
									scopeGroups[scopeGroupName] = scopeGroupConfig
								}
							}
						}
					}
					else -> {
						//判断配置文件中的顶级的key是否匹配"alias[?]"，如果匹配，则解析它的子属性（或它的值），添加到aliases中
						val aliasName = resolveAliasName(key)
						if(aliasName != null && aliasName.isNotEmpty()) {
							aliases[aliasName] = property
							continue
						}
						
						//其他情况，放到definition中
						val definitionName = key
						val definitionConfig = resolveDefinitionConfig(property, definitionName)
						if(definitionConfig != null) {
							definitions[definitionName] = definitionConfig
						}
					}
				}
			}
		}
		
		logger.info("Resolve config group '$name' finished.")
	}
	
	private fun resolveTypeConfig(config: CwtConfigProperty, name: String): CwtTypeConfig? {
		var path: String? = null
		var pathStrict = false
		var pathFile: String? = null
		var pathExtension: String? = null
		var nameField: String? = null
		var nameFromFile = false
		var typePerFile = false
		var unique = false
		var severity: String? = null
		val skipRootKey: MutableList<List<String>> = mutableListOf()
		val localisation: MutableMap<String, MutableList<CwtTypeLocalisationConfig>> = mutableMapOf()
		val subtypes: MutableMap<String, CwtSubtypeConfig> = mutableMapOf()
		var typeKeyFilter: ReversibleList<String>? = null
		var startsWith: String? = null
		var graphRelatedTypes: List<String>? = null
		
		val props = config.properties?:return null
		for(prop in props) {
			val key = prop.key
			when(key) {
				//这里path需要移除前缀"game/"，这个插件会忽略它
				"path" -> path = prop.stringValue?.removePrefix("game/") ?: continue
				"path_strict" -> pathStrict = prop.booleanValue ?: continue
				"path_file" -> pathFile = prop.stringValue ?: continue
				"path_extension" -> pathExtension = prop.stringValue ?: continue
				"name_field" -> nameField = prop.stringValue ?: continue
				"name_from_file" -> nameFromFile = prop.booleanValue ?: continue
				"type_per_file" -> typePerFile = prop.booleanValue ?: continue
				"unique" -> unique = prop.booleanValue ?: continue
				"severity" -> severity = prop.stringValue ?: continue
				"skip_root_key" -> {
					//值可能是string也可能是stringArray
					val list = prop.stringValueOrValues
					if(list != null) skipRootKey.add(list)
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
								localisation.put(subtypeName, localisationConfigs)
							} else {
								val localisationConfigs = localisation.getOrPut("") { mutableListOf() }
								val localisationConfig = resolveTypeLocalisationConfig(p, k) ?: continue
								localisationConfigs.add(localisationConfig)
							}
						}
					}
				}
			}
			
			val subtypeName = resolveSubtypeName(key)
			if(subtypeName != null) {
				val subtypeConfig = resolveSubtypeConfig(prop, subtypeName)
				if(subtypeConfig != null) {
					subtypes[subtypeName] = subtypeConfig
				}
			}
		}
		
		val options = config.options
		if(options != null) {
			for(option in options) {
				val key = option.key
				when(key) {
					"starts_with" -> startsWith = option.stringValue ?: continue
					"type_key_filter" -> {
						val reversed = option.separatorType == SeparatorType.NOT_EQUAL
						//值可能是string也可能是stringArray
						val list = option.stringValueOrValues
						typeKeyFilter = list?.toReversibleList(reversed)
					}
					"graph_related_types" -> {
						val optionValues = option.values ?: continue
						val list = optionValues.map { it.value }
						graphRelatedTypes = list
					}
				}
			}
		}
		
		return CwtTypeConfig(
			name, path, pathStrict, pathFile, pathExtension, nameField, nameFromFile, typePerFile, unique, severity, 
			skipRootKey, localisation, subtypes, typeKeyFilter, startsWith, graphRelatedTypes
		)
	}
	
	private fun resolveSubtypeConfig(config: CwtConfigProperty, name: String): CwtSubtypeConfig? {
		var typeKeyFilter:ReversibleList<String>? = null
		var pushScope:String? = null
		var startsWith:String? = null
		var displayName:String? = null
		var abbreviation: String? = null
		var onlyIfNot: List<String>? = null
		val options = config.options?:return null
		for(option in options) {
			val key = option.key
			when(key) {
				"type_key_filter" -> {
					val reversed = option.separatorType == SeparatorType.NOT_EQUAL
					//值可能是string也可能是stringArray
					val list = option.stringValueOrValues
					typeKeyFilter = list?.toReversibleList(reversed)
				}
				"push_scope" -> pushScope = option.stringValue
				"starts_with" -> startsWith = option.stringValue
				"display_name" -> displayName = option.stringValue
				"abbreviation" -> abbreviation = option.stringValue
				"only_if_not" -> onlyIfNot = option.stringValues
			}
		}
		return CwtSubtypeConfig(name, config,typeKeyFilter, pushScope, startsWith, displayName, abbreviation, onlyIfNot)
	}
	
	private fun resolveTypeLocalisationConfig(config: CwtConfigProperty, name: String): CwtTypeLocalisationConfig? {
		val expression = config.stringValue ?: return null
		var required = false
		var primary = false
		val optionValues = config.optionValues?:return null
		for(optionValue in optionValues) {
			val value = optionValue.stringValue ?: continue
			when(value) {
				"required" -> required = true
				"primary" -> primary = true
			}
		}
		return CwtTypeLocalisationConfig(name, expression, required, primary)
	}
	
	private fun resolveEnumConfig(config: CwtConfigProperty): List<String>? {
		return config.values?.map { it.value }
	}
	
	private fun resolveLinkConfig(config: CwtConfigProperty): CwtLinkConfig? {
		var inputscopes: List<String>? = null
		var output_scope: String? = null
		val props = config.properties
		if(props == null || props.isEmpty()) return null
		for(prop in props) {
			when(prop.key) {
				"inputscopes" -> inputscopes = prop.stringValues
				"output_scope" -> output_scope = prop.value
			}
		}
		if(inputscopes == null || output_scope == null) return null
		return CwtLinkConfig(inputscopes, output_scope)
	}
	
	private fun resolveLocalisationCommandConfig(config: CwtConfigProperty): List<String>? {
		return config.stringValueOrValues
	}
	
	private fun resolveLocalisationLinkConfig(config: CwtConfigProperty): CwtLinkConfig? {
		var inputscopes: List<String>? = null
		var output_scope: String? = null
		val props = config.properties
		if(props == null || props.isEmpty()) return null
		for(prop in props) {
			when(prop.key) {
				"input_scopes" -> inputscopes = prop.stringValues
				"output_scope" -> output_scope = prop.value
			}
		}
		if(inputscopes == null || output_scope == null) return null
		return CwtLinkConfig(inputscopes, output_scope)
	}
	
	private fun resolveModifierCategoryConfig(config: CwtConfigProperty): CwtModifyCategoryConfig? {
		var internal_id: Int? = null
		var supported_scopes: List<String>? = null
		val props = config.properties
		if(props == null || props.isEmpty()) return null
		for(prop in props) {
			when(prop.key) {
				"internal_id" -> internal_id = prop.intValue
				"supported_scopes" -> supported_scopes = prop.stringValues
			}
		}
		if(internal_id == null || supported_scopes == null) return null
		return CwtModifyCategoryConfig(internal_id, supported_scopes)
	}
	
	private fun resolveScopeGroupConfig(config: CwtConfigProperty): List<String>? {
		return config.stringValueOrValues
	}
	
	private fun resolveScopeConfig(config: CwtConfigProperty): CwtScopeConfig? {
		var aliases: List<String>? = null
		val props = config.properties
		if(props == null || props.isEmpty()) return null
		for(prop in props) {
			if(prop.key == "aliases") aliases = prop.stringValues
		}
		if(aliases == null) return null
		return CwtScopeConfig(aliases)
	}
	
	private fun resolveDefinitionConfig(config: CwtConfigProperty, name: String): CwtDefinitionConfig? {
		val props = config.properties
		val propertiesConfig = mutableMapOf<String, CwtConfigProperty>()
		val subtypePropertiesConfig = mutableMapOf<String, MutableMap<String, CwtConfigProperty>>()
		if(props == null) return null //不要排除props是空的情况
		for(prop in props) {
			val properties = prop.properties
			if(properties != null) {
				//这里需要进行合并
				val map = properties.associateBy { it.key }
				val subtypeName = resolveSubtypeName(prop.key)
				if(subtypeName != null) {
					val subtypeMap = subtypePropertiesConfig.getOrPut(name) { mutableMapOf() }
					subtypeMap.putAll(map)
				} else {
					propertiesConfig.putAll(map)
				}
			}
		}
		return CwtDefinitionConfig(name, propertiesConfig, subtypePropertiesConfig)
	}
	
	/**
	 * 根据指定的scriptProperty，匹配类型规则，得到对应的definitionInfo。
	 */
	fun resolveDefinitionInfo(element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPath): ParadoxDefinitionInfo? {
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
		//判断value是否是block
		val valueIsBlock = element.propertyValue?.value is ParadoxScriptBlock
		if(!valueIsBlock) return false
		
		//判断path是否匹配
		val pathConfig = typeConfig.path ?: return false
		val pathStrictConfig = typeConfig.pathStrict
		if(pathStrictConfig) {
			if(pathConfig != path.parent) return false
		} else {
			if(!pathConfig.matchesPath(path.parent)) return false
		}
		//判断path_name是否匹配
		val pathFileConfig = typeConfig.pathFile //String?
		if(pathFileConfig != null) {
			if(pathFileConfig != path.fileName) return false
		}
		//判断path_extension是否匹配
		val pathExtensionConfig = typeConfig.pathExtension //String?
		if(pathExtensionConfig != null) {
			if(pathExtensionConfig != path.fileExtension) return false
		}
		//如果skip_root_key = <any>，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过
		//skip_root_key可以为列表（多级），可以重复（其中之一匹配即可）
		val skipRootKeyConfig = typeConfig.skipRootKey //String? | "any"
		if(skipRootKeyConfig.isEmpty()) {
			if(propertyPath.length != 1) return false
		} else {
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
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = if(typeKeyFilterConfig.reverse) {
				elementName !in typeKeyFilterConfig
			} else {
				elementName in typeKeyFilterConfig
			}
			if(!filterResult) return false
		}
		return true
	}
	
	private fun matchesSubtype(subtypeConfig: CwtSubtypeConfig, element: ParadoxScriptProperty, elementName: String, result: MutableList<CwtSubtypeConfig>): Boolean {
		//如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
		val onlyIfNotConfig = subtypeConfig.onlyIfNot
		if(onlyIfNotConfig != null && onlyIfNotConfig.isNotEmpty()) {
			val matchesAny = result.any { it.name in onlyIfNotConfig }
			if(matchesAny) return false
		}
		
		//如果type_key_filter存在，则过滤key
		val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = if(typeKeyFilterConfig.reverse) {
				elementName !in typeKeyFilterConfig
			} else {
				elementName in typeKeyFilterConfig
			}
			if(!filterResult) return false
		}
		//如果starts_with存在，则要求elementName匹配这个前缀
		val startsWithConfig = subtypeConfig.startsWith
		if(startsWithConfig != null && startsWithConfig.isNotEmpty()) {
			if(!elementName.startsWith(startsWithConfig)) return false
		}
		//根据config对property进行内容匹配
		val elementConfig = subtypeConfig.config
		return matchContent(element, elementConfig, this)
	}
	
	private fun toDefinitionInfo(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String): ParadoxDefinitionInfo {
		val name = getName(typeConfig, element, elementName)
		val typeKey = elementName
		val type = typeConfig.name
		val subtypesConfig = getSubtypesConfig(typeConfig, element, elementName)
		val subtypes = subtypesConfig.map { it.name }
		val localisationConfig = getLocalisationConfig(typeConfig, subtypes)
		val localisation = getLocalisation(localisationConfig, element, name)
		val graphRelatedTypes = typeConfig.graphRelatedTypes.orEmpty()
		val unique = typeConfig.unique
		val severity = typeConfig.severity
		val pushScopes = subtypesConfig.map { it.pushScope }
		return ParadoxDefinitionInfo(
			name, typeKey, type, subtypes, subtypesConfig, localisation, localisationConfig,
			graphRelatedTypes, unique, severity, pushScopes
		)
	}
	
	private fun getName(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String): String {
		//如果name_from_file = yes，则返回文件名（不包含扩展）
		val nameFromFileConfig = typeConfig.nameFromFile
		if(nameFromFileConfig) return element.containingFile.name.substringBeforeLast('.')
		//如果name_field = <any>，则返回对应名字的property的value
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) return element.findProperty(nameFieldConfig, true)?.value.orEmpty()
		//如果有一个子属性的propertyKey为name，那么取这个子属性的值，这是为了兼容cwt规则文件尚未考虑到的一些需要名字的情况
		val nameProperty = element.findProperty("name", true)
		if(nameProperty != null) return nameProperty.value.orEmpty()
		//否则直接返回elementName
		return elementName
	}
	
	private fun getSubtypesConfig(typeConfig: CwtTypeConfig, element: ParadoxScriptProperty, elementName: String): List<CwtSubtypeConfig> {
		val subtypesConfig = typeConfig.subtypes
		val result = mutableListOf<CwtSubtypeConfig>()
		for(subtypeConfig in subtypesConfig.values) {
			if(matchesSubtype(subtypeConfig, element, elementName, result)) result.add(subtypeConfig)
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
	
	private fun getLocalisation(localisationConfig: List<CwtTypeLocalisationConfig>, element: ParadoxScriptProperty, name: String): List<ParadoxDefinitionLocalisationInfo> {
		val result = mutableListOf<ParadoxDefinitionLocalisationInfo>()
		//从已有的cwt规则
		for(config in localisationConfig) {
			//如果name为空，则keyName也为空 
			//如果expression包含"$"，则keyName为将expression中的"$"替换为name后得到的字符串
			//否则，keyName为expression对应的definition的同名子属性（不区分大小写）的值对应的字符串
			val expression = config.expression
			val keyName = resolveKeyName(name, expression, element)
			val info = ParadoxDefinitionLocalisationInfo(config.name, keyName, config.required, config.primary)
			result.add(info)
		}
		//从推断的cwt规则
		val names = localisationConfig.map { it.name.lowercase() }
		for(inferredName in definitionLocalisationNamesToInfer) {
			val inferredKeyName = inferKeyName(inferredName, names, element)
			if(inferredKeyName != null) {
				val info = ParadoxDefinitionLocalisationInfo(inferredName, inferredKeyName)
				result.add(info)
			}
		}
		return result
	}
	
	private fun resolveKeyName(name: String, expression: String, element: ParadoxScriptProperty): String {
		return when {
			name.isEmpty() -> ""
			expression.contains('$') -> buildString { for(c in expression) if(c == '$') append(name) else append(c) }
			else -> element.findProperty(expression, ignoreCase = true)?.propertyValue?.value
				.castOrNull<ParadoxScriptString>()?.stringValue ?: ""
		}
	}
	
	private fun inferKeyName(name: String, names: List<String>, element: ParadoxScriptProperty): String? {
		if(name !in names) {
			return element.findProperty(name, ignoreCase = true)?.propertyValue?.value
				.castOrNull<ParadoxScriptString>()?.stringValue
		}
		return null
	}
}


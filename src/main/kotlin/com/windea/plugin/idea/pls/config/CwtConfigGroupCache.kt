package com.windea.plugin.idea.pls.config

import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.model.*
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

class CwtConfigGroupCache(val configGroup: Map<String, CwtConfig>, val gameType: ParadoxGameType,val name:String) {
	companion object {
		private val logger = LoggerFactory.getLogger(CwtConfigGroupCache::class.java)
		
		const val typesKey = "types"
		const val typeKeyPrefix = "type["
		const val typeKeySuffix = "]"
		const val enumsKey = "enums"
		const val enumKeyPrefix = "enum["
		const val enumKeySuffix = "]"
		const val aliasKeyPrefix = "alias["
		const val aliasKeySuffix = "]"
		const val modifierPrefix = "modifier:"
		const val effectPrefix = "effect:"
		const val scopesKey = "scopes"
		const val scopeGroupsKey = "scope_groups"
		
		private fun resolveTypeKey(key: String) = key.resolveByRemoveSurrounding(typeKeyPrefix, typeKeySuffix)
		private fun resolveEnumKey(key:String) = key.resolveByRemoveSurrounding(enumKeyPrefix, enumKeySuffix)
		private fun resolveAliasKey(key:String) = key.resolveByRemoveSurrounding(aliasKeySuffix, aliasKeySuffix)
	}
	
	//? -> alias[?] = ...
	//? -> alias[?] = { ... }
	val aliases:Map<String,CwtConfigProperty>
	
	//? -> type[?] = { ... }
	//TODO 进一步解析
	val types: Map<String, CwtConfigProperty>
	
	//? -> [a, b, 1, 2, yes]
	//枚举值也有可能是int、number、bool类型，这里统一用字符串表示
	val enums: Map<String,List<String>>
	
	init {
		logger.info("Resolve config group '$name'...")
		
		types = ConcurrentHashMap()
		enums = ConcurrentHashMap()
		aliases = ConcurrentHashMap()
		
		for((_, config) in configGroup) {
			for(property in config.properties) {
				val propertyKey = property.key
				when(propertyKey) {
					//找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
					typesKey -> {
						val typeProperties = property.properties
						if(typeProperties != null && typeProperties.isNotEmpty()){
							for(typeProperty in typeProperties){
								val typeName = resolveTypeKey(typeProperty.key)
								if(typeName != null && typeName.isNotEmpty()) {
									types[typeName] = typeProperty
								}
							}
						}
						continue
					}
					//找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums中
					enumsKey -> {
						val enumProperties = property.properties
						if(enumProperties != null && enumProperties.isNotEmpty()){
							for(enumProperty in enumProperties){
								val enumName = resolveEnumKey(enumProperty.key)
								if(enumName != null && enumName.isNotEmpty()) {
									val enumPropertyValues = enumProperty.values
									//不排除enumPropertyValues.isEmpty()的情况，作为占位符
									if(enumPropertyValues != null){
										val enumValues = enumPropertyValues.mapNotNull { it.value }	
										enums[enumName] = enumValues
									}
								}
							}
						}
						continue
					}
				}
				//判断配置文件中的顶级的key是否匹配"alias[?]"，如果匹配，则解析它的子属性（或它的值），添加到aliases中
				val aliasName = resolveAliasKey(propertyKey)
				if(aliasName != null){
					if(aliasName.isEmpty()) continue //忽略aliasName为空字符串的情况
					val aliasProperty = property
					aliases[aliasName] = aliasProperty
				}
			}
		}
		
		logger.info("Resolve config group '$name' finished.")
	}
	
	///**
	// * @property name_field propertyKey
	// * @property path path
	// * @property subtypes subtypeInfoMap
	// * @property localisation localisationInfo
	// */
	//data class CwtConfigType(
	//	val name_field:String,
	//	val path:String,
	//	val subtypes: Map<String,List<CwtConfigProperty>>,
	//	val localisation:List<CwtConfigProperty>
	//)
}
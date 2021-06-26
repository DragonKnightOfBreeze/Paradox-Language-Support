package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.cwt.config.*
import java.util.*

data class ParadoxDefinitionInfo(
	val name: String,
	val type: String,
	val typeConfig: CwtTypeConfig,
	val subtypes: List<String>,
	val subtypesConfig: List<CwtSubtypeConfig>,
	val localisation: List<ParadoxDefinitionLocalisationInfo>,
	val localisationConfig: List<CwtTypeLocalisationConfig>,
	val definitionConfig: List<CwtPropertyConfig>,
	val typeKey: String,
	val graphRelatedTypes: List<String>,
	val unique: Boolean,
	val severity: String?,
	val pushScopes: List<String?>,
	val gameType: ParadoxGameType
) {
	val types = mutableListOf(type).apply { addAll(subtypes) }
	val subtypeText = subtypes.joinToString(", ")
	val typeText = types.joinToString(", ")
	val typeLinkText = buildString {
		val typeLink = "@${gameType.key}.types.$type"
		appendPsiLink(typeLink, type)
		for(subtype in subtypes) {
			append(", ")
			appendPsiLink("$typeLink.$subtype", subtype)
		}
	}
	val typePointer = typeConfig.pointer
	val subtypesPointer = subtypesConfig.map { it.pointer }
	val typesPointer = mutableListOf(typePointer).apply { addAll(subtypesPointer) }
	val localisationNames = localisation.map { it.name }
	val localisationKeyNames = localisation.map { it.keyName }
	
	private val propertyConfigsCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	private val childPropertyConfigsCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	private val childValueConfigsCache = WeakHashMap<String,List<CwtValueConfig>>()
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo && name == other.name && types == other.types
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, types)
	}
	
	/**
	 * 判断是否匹配指定的类型表达式（`type.subtype`）。
	 */
	fun matchesTypeExpression(typeExpression: String): Boolean {
		val dotIndex = typeExpression.indexOf('.')
		val type = if(dotIndex == -1) typeExpression else typeExpression.substring(0, dotIndex)
		val subtype = if(dotIndex == -1) null else typeExpression.substring(dotIndex + 1)
		return type == this.type && (subtype == null || subtype in subtypes)
	}
	
	/**
	 * 根据路径解析对应的属性配置列表。
	 */
	fun resolvePropertyConfigs(path: ParadoxPropertyPath,configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = path.toString()
		return propertyConfigsCache.getOrPut(cacheKey){
			when {
				//这里的属性路径不应该为空
				path.isEmpty() -> emptyList() 
				else -> {
					var result = definitionConfig
					var isTop = true
					for((key, quoted) in path.subPathInfos) {
						if(isTop) isTop = false else result = result.flatMap { it.properties?: emptyList() }
						result = result.filter { matchesKey(it.keyExpression, key, quoted, configGroup) }
					}
					result
				}
			}
		}
	}
	
	/**
	 * 根据路径解析对应的子属性配置列表。（过滤重复的）
	 */
	fun resolveChildPropertyConfigs(path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = path.toString()
		return childPropertyConfigsCache.getOrPut(cacheKey){
			when {
				//这里的属性路径可以为空，这时得到的就是顶级属性列表
				path.isEmpty() -> definitionConfig
				else -> resolvePropertyConfigs(path, configGroup).flatMap { it.properties?:emptyList() }
			}
		}.distinctBy { it.key }
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	fun resolveChildValuesConfigs(path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtValueConfig> {
		val cacheKey = path.toString()
		return childValueConfigsCache.getOrPut(cacheKey){
			when {
				//这里的属性路径可以为空，这时得到的是空列表（假定在顶级的是属性不是值）
				path.isEmpty() -> emptyList() 
				else -> resolvePropertyConfigs(path, configGroup).flatMap { it.values?:emptyList() }
			}
		}.distinctBy{it.value}
	}
}
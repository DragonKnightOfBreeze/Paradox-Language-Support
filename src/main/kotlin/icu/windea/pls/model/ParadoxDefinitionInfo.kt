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
	
	private val resolveDefinitionConfigCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	private val resolveSubDefinitionConfigCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	
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
	 * 根据路径解析定义配置。
	 */
	fun resolveDefinitionConfig(path: ParadoxPropertyPath,configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = path.toString()
		return resolveDefinitionConfigCache.getOrPut(cacheKey){
			when {
				path.isEmpty() -> {
					emptyList() //这里的属性路径不应该为空
				}
				else -> {
					var result = definitionConfig
					var isTop = true
					for((key, quoted) in path.subPathInfos) {
						if(isTop) isTop = false else result = result.flatMap { it.properties?: emptyList() }
						result = result.filter { matchesKey(it.key, key, quoted, configGroup) }
					}
					result
				}
			}
		}
	}
	
	/**
	 * 根据路径解析子定义配置。
	 */
	fun resolveSubDefinitionConfig(path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = path.toString()
		return resolveSubDefinitionConfigCache.getOrPut(cacheKey){
			when {
				path.isEmpty() -> {
					definitionConfig.distinctBy { it.key }
				}
				else -> {
					var result = definitionConfig
					for((key, quoted) in path.subPathInfos) {
						result = result.filter { matchesKey(it.key, key, quoted, configGroup) }
							.flatMap { it.properties ?: emptyList() }
					}
					result.distinctBy { it.key }
				}
			}
		}
	}
}
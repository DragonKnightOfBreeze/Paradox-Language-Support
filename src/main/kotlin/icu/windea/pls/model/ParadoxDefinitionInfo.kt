package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*

data class ParadoxDefinitionInfo(
	val name: String,
	val type: String, // = typeConfig.name
	val typeConfig: CwtTypeConfig,
	val subtypes: List<String>, // = subtypeConfigs.map { it.name }
	val subtypeConfigs: List<CwtSubtypeConfig>,
	val localisation: List<ParadoxRelatedLocalisationInfo>,
	val localisationConfig: CwtTypeLocalisationConfig?,
	val definition: List<CwtPropertyConfig>,
	val definitionConfig: CwtDefinitionConfig?,
	val typeKey: String,
	val graphRelatedTypes: List<String>, // = typeConfig.graphRelatedTypes.orEmpty()
	val unique: Boolean, // = typeConfig.unique
	val severity: String?, // = typeConfig.severity
	val gameType: ParadoxGameType
) {
	val types = mutableListOf(type).apply { addAll(subtypes) }
	val typeText = types.joinToString(", ")
	val localisationNames = localisation.map { it.name }
	val localisationKeyNames = localisation.map { it.keyName }
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo && name == other.name && types == other.types
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, types)
	}
	
	/**
	 * 根据路径解析对应的属性配置列表。
	 */
	fun resolvePropertyConfigs(path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		return definitionConfig?.resolvePropertyConfigs(subtypes, path, configGroup) ?: emptyList()
	}
	
	/**
	 * 根据路径解析对应的子属性配置列表。（过滤重复的）
	 */
	fun resolveChildPropertyConfigs(path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		return definitionConfig?.resolveChildPropertyConfigs(subtypes, path, configGroup) ?: emptyList()
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	fun resolveChildValuesConfigs(path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtValueConfig> {
		return definitionConfig?.resolveChildValuesConfigs(subtypes, path, configGroup) ?: emptyList()
	}
	
	fun resolveChildPropertyOccurrence(childPropertyConfigs: List<CwtPropertyConfig>, element: ParadoxDefinitionProperty, configGroup: CwtConfigGroup): Map<CwtKeyExpression, Int> {
		val properties = element.properties
		if(properties.isEmpty()) return emptyMap()
		return properties.groupAndCountBy { prop ->
			childPropertyConfigs.find { matchesKey(it.keyExpression, prop.propertyKey, configGroup) }?.keyExpression
		}
	}
	
	fun resolveChildValueOccurrence(childValueConfigs: List<CwtValueConfig>, element: ParadoxDefinitionProperty, configGroup: CwtConfigGroup): Map<CwtValueExpression, Int> {
		val values = element.values
		if(values.isEmpty()) return emptyMap()
		return values.groupAndCountBy { value ->
			childValueConfigs.find { matchesValue(it.valueExpression, value, configGroup) }?.valueExpression
		}
	}
}
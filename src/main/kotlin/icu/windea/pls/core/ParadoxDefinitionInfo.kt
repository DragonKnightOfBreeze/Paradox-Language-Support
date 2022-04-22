package icu.windea.pls.core

import icu.windea.pls.config.cwt.config.*
import java.util.*

class ParadoxDefinitionInfo(
	val name: String,
	val type: String, // = typeConfig.name
	val typeConfig: CwtTypeConfig,
	val subtypes: List<String>, // = subtypeConfigs.map { it.name }
	val subtypeConfigs: List<CwtSubtypeConfig>,
	val localisation: List<ParadoxRelatedLocalisationInfo>,
	val localisationConfig: CwtTypeLocalisationConfig?,
	val definition: List<CwtPropertyConfig>,
	val definitionConfig: CwtDefinitionConfig?,
	val rootKey: String,
	val gameType: ParadoxGameType
) {
	val types: List<String> by lazy { mutableListOf(type).apply { addAll(subtypes) } }
	val typeText: String by lazy { types.joinToString(", ") }
	val localisationNames: List<String> by lazy { localisation.map { it.name } }
	val localisationKeyNames: List<String> by lazy { localisation.map { it.keyName } }
	
	val graphRelatedTypes get() = typeConfig.graphRelatedTypes
	val unique get() = typeConfig.unique
	val severity get() = typeConfig.severity
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo && name == other.name && types == other.types
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, types)
	}
}
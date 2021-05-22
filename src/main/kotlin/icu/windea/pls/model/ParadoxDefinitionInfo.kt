package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.script.psi.*
import java.util.*

@Suppress("UNCHECKED_CAST")
data class ParadoxDefinitionInfo(
	val name: String,
	val type: String,
	val subtypes: List<String> = emptyList(),
	val subtypesConfig: List<CwtSubtypeConfig> = emptyList(),
	val localisation: List<ParadoxLocalisationInfo> = emptyList(),
	val localisationConfig: List<CwtTypeLocalisationConfig> = emptyList(),
	val graphRelatedTypes: List<String> = emptyList(),
	val unique: Boolean = false,
	val severity: String? = null,
	val pushScopes: List<String?> = emptyList()
) {
	val types = mutableListOf(type).apply { addAll(subtypes) }
	val typeText = types.joinToString(", ")
	val localisationNames = localisation.map { it.name }
	val localisationKeyNames = localisation.map{ it.keyName }
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo && name == other.name && types == other.types
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, types)
	}
	
	override fun toString(): String {
		return "$name: $typeText"
	}
}
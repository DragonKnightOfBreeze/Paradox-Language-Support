package icu.windea.pls.model

import icu.windea.pls.config.*
import java.util.*

data class ParadoxDefinitionInfo(
	val name: String,
	val typeKey: String,
	val type: String,
	val subtypes: List<String> = emptyList(),
	val subtypesConfig: List<CwtSubtypeConfig> = emptyList(),
	val localisation: List<ParadoxDefinitionLocalisationInfo> = emptyList(),
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
		return "(definition) $name: $typeText"
	}
	
	/**
	 * 判断是否匹配指定的类型表达式（`type.subtype`）。
	 */
	fun matchesTypeExpression(typeExpression:String):Boolean{
		val dotIndex = typeExpression.indexOf('.')
		val type = if(dotIndex == -1) typeExpression else typeExpression.substring(0,dotIndex)
		val subtype = if(dotIndex == -1) null else typeExpression.substring(dotIndex+1)
		return type == this.type && (subtype == null || subtype in subtypes)
	}
}
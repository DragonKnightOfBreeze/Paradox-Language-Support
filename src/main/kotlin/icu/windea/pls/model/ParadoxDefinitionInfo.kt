package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.cwt.config.*
import java.util.*

data class ParadoxDefinitionInfo(
	val name: String,
	val typeKey: String,
	val type: String,
	val typeConfig: CwtTypeConfig,
	val subtypes: List<String>,
	val subtypesConfig: List<CwtSubtypeConfig>,
	val localisation: List<ParadoxDefinitionLocalisationInfo>,
	val localisationConfig: List<CwtTypeLocalisationConfig>,
	val graphRelatedTypes: List<String>,
	val unique: Boolean,
	val severity: String?,
	val pushScopes: List<String?>,
	val fileInfo: ParadoxFileInfo
) {
	val types = mutableListOf(type).apply { addAll(subtypes) }
	val typeText = types.joinToString(", ")
	val typeLinkText = buildString {
		val gameType = fileInfo.gameType.key
		val typeLink = "@$gameType.types.$type"
		appendPsiLink(typeLink, type)
		for(subtype in subtypes) {
			append(", ")
			appendPsiLink("$typeLink.$subtype", subtype)
		}
	}
	val typePointer = typeConfig.pointer
	val subtypesPointer = subtypesConfig.mapNotNull { it.pointer }
	val typesPointer = if(typePointer == null) subtypesPointer else mutableListOf(typePointer).apply { addAll(subtypesPointer) }
	
	val localisationNames = localisation.map { it.name }
	val localisationKeyNames = localisation.map { it.keyName }
	
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
}
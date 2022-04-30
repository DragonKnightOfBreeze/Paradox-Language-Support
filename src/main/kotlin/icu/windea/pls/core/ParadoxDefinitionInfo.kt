package icu.windea.pls.core

import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * @property elementPath 相对于所属文件的属性路径。
 */
@Suppress("unused")
class ParadoxDefinitionInfo(
	val name: String,
	val type: String, // = typeConfig.name
	val typeConfig: CwtTypeConfig,
	val subtypes: List<String>, // = subtypeConfigs.map { it.name }
	val subtypeConfigs: List<CwtSubtypeConfig>,
	val localisation: List<ParadoxRelatedLocalisationInfo>,
	val localisationConfig: CwtTypeLocalisationConfig?,
	val pictures: List<ParadoxRelatedPicturesInfo>,
	val picturesConfig: CwtTypePicturesConfig?,
	val definition: List<CwtPropertyConfig>,
	val definitionConfig: CwtDefinitionConfig?,
	val rootKey: String,
	val elementPath: ParadoxDefinitionPath,
	val gameType: ParadoxGameType
) {
	val types: List<String> by lazy { mutableListOf(type).apply { addAll(subtypes) } }
	val typesText: String by lazy { types.joinToString(", ") }
	val primaryLocalisation: ParadoxRelatedLocalisationInfo? by lazy { localisation.firstOrNull { it.primary } }
	val primaryPicture: ParadoxRelatedPicturesInfo? by lazy { pictures.firstOrNull { it.primary } }
	
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
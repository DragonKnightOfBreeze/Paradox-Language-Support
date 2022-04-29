package icu.windea.pls.core

import com.intellij.openapi.project.Project
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.localisation.psi.*
import java.util.*

/**
 * @property primaryLocalisationLocation 用作本地化名字的本地化的位置（即本地化的键，localisationKey）。
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
	val definition: List<CwtPropertyConfig>,
	val definitionConfig: CwtDefinitionConfig?,
	val rootKey: String,
	val gameType: ParadoxGameType
) {
	val types: List<String> by lazy { mutableListOf(type).apply { addAll(subtypes) } }
	val typesText: String by lazy { types.joinToString(", ") }
	val primaryLocalisationLocation: String? by lazy { localisation.firstOrNull { it.primary }?.location }
	val localisationLocations: List<String> by lazy { localisation.map { it.location } }
	
	val graphRelatedTypes get() = typeConfig.graphRelatedTypes
	val unique get() = typeConfig.unique
	val severity get() = typeConfig.severity
	
	fun getPrimaryLocalisation(project: Project): ParadoxLocalisationProperty? {
		return primaryLocalisationLocation?.let { findLocalisation(it, null, project, hasDefault = true) }
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo && name == other.name && types == other.types
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, types)
	}
}
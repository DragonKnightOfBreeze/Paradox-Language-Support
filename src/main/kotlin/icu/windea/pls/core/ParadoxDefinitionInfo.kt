package icu.windea.pls.core

import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * @property elementPath 相对于所属文件的属性路径。
 */
@Suppress("unused")
class ParadoxDefinitionInfo(
	val rootKey: String,
	val typeConfig: CwtTypeConfig,
	val elementPath: ParadoxElementPath<ParadoxScriptFile>,
	val gameType: ParadoxGameType,
	val configGroup: CwtConfigGroup,
	element: ParadoxDefinitionProperty //直接传入element
) {
	val type: String = typeConfig.name
	
	//NOTE 部分属性需要使用懒加载
	
	val name: String by lazy {
		//如果name_from_file = yes，则返回文件名（不包含扩展）
		val nameFromFileConfig = typeConfig.nameFromFile
		if(nameFromFileConfig) return@lazy element.containingFile.name.substringBeforeLast('.')
		//如果name_field = <any>，则返回对应名字的property的value
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) return@lazy element.findProperty(nameFieldConfig, true)?.value.orEmpty()
		//否则直接返回rootKey
		rootKey
	}
	
	val subtypeConfigs: List<CwtSubtypeConfig> by lazy {
		val subtypesConfig = typeConfig.subtypes
		val result = SmartList<CwtSubtypeConfig>()
		for(subtypeConfig in subtypesConfig.values) {
			if(configGroup.matchesSubtype(subtypeConfig, element, rootKey, result)) result.add(subtypeConfig)
		}
		result
	}
	
	val subtypes: List<String> by lazy {
		subtypeConfigs.map { it.name }
	}
	
	val localisation: List<ParadoxRelatedLocalisationInfo> by lazy {
		val mergedLocalisationConfig = typeConfig.localisation?.getMergedConfigs(subtypes) ?: return@lazy emptyList()
		val result = SmartList<ParadoxRelatedLocalisationInfo>()
		//从已有的cwt规则
		for(config in mergedLocalisationConfig) {
			val locationExpression = CwtLocalisationLocationExpression.resolve(config.expression)
			val info = ParadoxRelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
			result.add(info)
		}
		result
	}
	
	val pictures: List<ParadoxRelatedPicturesInfo> by lazy {
		val mergedPicturesConfig = typeConfig.pictures?.getMergedConfigs(subtypes) ?: return@lazy emptyList()
		val result = SmartList<ParadoxRelatedPicturesInfo>()
		//从已有的cwt规则
		for(config in mergedPicturesConfig) {
			val locationExpression = CwtPictureLocationExpression.resolve(config.expression)
			val info = ParadoxRelatedPicturesInfo(config.key, locationExpression, config.required, config.primary)
			result.add(info)
		}
		result
	}
	
	val definition: List<CwtPropertyConfig> by lazy {
		configGroup.definitions.get(type)?.getMergedConfigs(subtypes) ?: emptyList()
	}
	
	val types: List<String> by lazy {
		mutableListOf(type).apply { addAll(subtypes) }
	}
	
	val typeText: String by lazy {
		types.joinToString(", ")
	}
	
	val primaryLocalisationConfigs: List<ParadoxRelatedLocalisationInfo> by lazy {
		localisation.filter { it.primary || it.key.equals("name", true) || it.key.equals("title", true) } //TODO 额外进行一些推断，考虑可配置
	}
	
	val primaryPictureConfigs: List<ParadoxRelatedPicturesInfo> by lazy {
		pictures.filter { it.primary }
	}
	
	val typeCount get() = types.size
	val localisationConfig get() = typeConfig.localisation
	val picturesConfig get() = typeConfig.pictures
	val definitionConfig get() = configGroup.definitions.get(type)
	val graphRelatedTypes get() = typeConfig.graphRelatedTypes
	val unique get() = typeConfig.unique
	val severity get() = typeConfig.severity
	
	fun resolvePrimaryLocalisation(element: ParadoxDefinitionProperty): ParadoxLocalisationProperty? {
		if(primaryLocalisationConfigs.isEmpty()) return null //没有或者CWT规则不完善
		for(primaryLocalisationConfig in primaryLocalisationConfigs) {
			val resolved = primaryLocalisationConfig.locationExpression.resolve(name, element, inferParadoxLocale(), configGroup.project) ?: continue
			val localisation = resolved.second
			if(localisation != null) return localisation
		}
		return null
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo
			&& rootKey == other.rootKey && elementPath == other.elementPath && gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		return Objects.hash(rootKey, elementPath, gameType)
	}
}
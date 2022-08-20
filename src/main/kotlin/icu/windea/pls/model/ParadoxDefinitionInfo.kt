package icu.windea.pls.model

import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.selector.*

/**
 * @property fromMagicComment 此定义信息是否来自特殊注释。如果是，不加入索引。
 */
class ParadoxDefinitionInfo(
	val rootKey: String,
	val typeConfig: CwtTypeConfig,
	val gameType: ParadoxGameType,
	val configGroup: CwtConfigGroup,
	element: ParadoxDefinitionProperty, //直接传入element
) {
	var fromMagicComment: Boolean = false
	
	val type: String = typeConfig.name
	
	//NOTE 部分属性需要使用懒加载
	
	val name: String by lazy {
		//如果name_from_file = yes，则返回文件名（不包含扩展）
		val nameFromFileConfig = typeConfig.nameFromFile
		if(nameFromFileConfig) return@lazy element.containingFile.name.substringBeforeLast('.')
		//如果name_field = <any>，则返回对应名字的property的value
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) return@lazy element.findTargetElement(nameFieldConfig, true)?.value.orEmpty()
		//否则直接返回rootKey
		rootKey
	}
	
	val subtypes: List<String> by lazy {
		subtypeConfigs.map { it.name }
	}
	
	val subtypeConfigs: List<CwtSubtypeConfig> by lazy {
		val subtypesConfig = typeConfig.subtypes
		val result = SmartList<CwtSubtypeConfig>()
		for(subtypeConfig in subtypesConfig.values) {
			if(configGroup.matchesSubtype(subtypeConfig, element, rootKey, result)) result.add(subtypeConfig)
		}
		result
	}
	
	val types: List<String> by lazy {
		mutableListOf(type).apply { addAll(subtypes) }
	}
	
	val typesText: String by lazy {
		types.joinToString(", ")
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
	
	val images: List<ParadoxRelatedImageInfo> by lazy {
		val mergedImagesConfig = typeConfig.images?.getMergedConfigs(subtypes) ?: return@lazy emptyList()
		val result = SmartList<ParadoxRelatedImageInfo>()
		//从已有的cwt规则
		for(config in mergedImagesConfig) {
			val locationExpression = CwtImageLocationExpression.resolve(config.expression)
			val info = ParadoxRelatedImageInfo(config.key, locationExpression, config.required, config.primary)
			result.add(info)
		}
		result
	}
	
	val declaration: List<CwtKvConfig<*>> by lazy {
		configGroup.declarations.get(type)?.getMergedConfigs(subtypes) ?: emptyList()
	}
	
	val primaryLocalisationConfigs: List<ParadoxRelatedLocalisationInfo> by lazy {
		localisation.filter { it.primary || it.inferIsPrimary() }
	}
	
	val primaryImageConfigs: List<ParadoxRelatedImageInfo> by lazy {
		images.filter { it.primary || it.inferIsPrimary() }
	}
	
	val localisationConfig get() = typeConfig.localisation
	
	val imagesConfig get() = typeConfig.images
	
	val declarationConfig get() = configGroup.declarations.get(type)
	
	fun resolvePrimaryLocalisation(element: ParadoxDefinitionProperty): ParadoxLocalisationProperty? {
		if(primaryLocalisationConfigs.isEmpty()) return null //没有或者CWT规则不完善
		for(primaryLocalisationConfig in primaryLocalisationConfigs) {
			val selector = localisationSelector().gameTypeFrom(element).preferRootFrom(element).preferLocale(preferredParadoxLocale())
			val resolved = primaryLocalisationConfig.locationExpression.resolve(name, element, configGroup.project, selector = selector) ?: continue
			val localisation = resolved.second
			if(localisation != null) return localisation
		}
		return null
	}
}

@InferMethod
private fun ParadoxRelatedLocalisationInfo.inferIsPrimary(): Boolean {
	return name.equals("name", true) || name.equals("title", true)
}

@InferMethod
private fun ParadoxRelatedImageInfo.inferIsPrimary(): Boolean {
	return name.equals("icon", true)
}
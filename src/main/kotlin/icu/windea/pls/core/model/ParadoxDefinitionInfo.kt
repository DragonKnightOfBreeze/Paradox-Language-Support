package icu.windea.pls.core.model

import com.intellij.openapi.application.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.ParadoxDefinitionInfo.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * @property name 定义的名字。如果是空字符串，则表示定义是匿名的。（注意：不一定与定义的顶级键名相同，例如，可能来自某个属性的值）
 * @property rootKey 定义的顶级键名。（注意：不一定是定义的名字）
 * @property sourceType 此定义信息来自哪种解析方式。
 */
class ParadoxDefinitionInfo(
	val rootKey: String,
	val typeConfig: CwtTypeConfig,
	val gameType: ParadoxGameType,
	val configGroup: CwtConfigGroup,
	element: ParadoxDefinitionProperty, //直接传入element
) {
	enum class SourceType { Default, Stub, PathComment, TypeComment }
	
	var sourceType: SourceType = SourceType.Default
	
	val type: String = typeConfig.name
	
	//NOTE 部分属性需要使用懒加载
	
	val name: String by lazy {
		//name_from_file = yes -> 返回文件名（不包含扩展名）
		val nameFromFileConfig = typeConfig.nameFromFile
		if(nameFromFileConfig) return@lazy element.containingFile.name.substringBeforeLast('.')
		//name_field = xxx -> 返回对应名字（xxx）的property的value，如果不存在则返回空字符串
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) return@lazy element.findProperty(nameFieldConfig, true)?.value.orEmpty()
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
			if(ParadoxDefinitionInfoHandler.matchesSubtype(configGroup, subtypeConfig, element, rootKey, result)) result.add(subtypeConfig)
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
	
	val project get() = configGroup.project
	
	fun resolvePrimaryLocalisation(element: ParadoxDefinitionProperty): ParadoxLocalisationProperty? {
		if(primaryLocalisationConfigs.isEmpty()) return null //没有或者CWT规则不完善
		return runReadAction {
			for(primaryLocalisationConfig in primaryLocalisationConfigs) {
				val selector = localisationSelector().gameTypeFrom(element).preferRootFrom(element).preferLocale(preferredParadoxLocale())
				val resolved = primaryLocalisationConfig.locationExpression.resolve(element, this, configGroup.project, selector = selector) ?: continue
				val localisation = resolved.second
				if(localisation != null)  return@runReadAction localisation
			}
			null
		}
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo
			&& name == other.name && typesText == other.typesText && gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, typesText, gameType)
	}
}

/**
 * 定义是否需要进行索引。
 */
val ParadoxDefinitionInfo.shouldIndex: Boolean get() = sourceType == SourceType.PathComment || sourceType == SourceType.TypeComment

/**
 * 对应的定义是否是匿名的。
 */
val ParadoxDefinitionInfo.isAnonymous: Boolean get() = name.isEmpty()

@InferMethod
private fun ParadoxRelatedLocalisationInfo.inferIsPrimary(): Boolean {
	return name.equals("name", true) || name.equals("title", true)
}

@InferMethod
private fun ParadoxRelatedImageInfo.inferIsPrimary(): Boolean {
	return name.equals("icon", true)
}
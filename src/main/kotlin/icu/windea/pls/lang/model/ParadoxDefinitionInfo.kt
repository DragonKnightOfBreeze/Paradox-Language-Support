package icu.windea.pls.lang.model

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import java.util.*
import java.util.concurrent.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * @property elementPath 相对于所属文件的定义成员路径。
 * @property name 定义的名字。如果是空字符串，则表示定义是匿名的。（注意：不一定与定义的顶级键名相同，例如，可能来自某个属性的值）
 * @property rootKey 定义的顶级键名。（注意：不一定是定义的名字）
 * @property sourceType 此定义信息来自哪种解析方式。
 */
class ParadoxDefinitionInfo(
    name0: String?, // null -> lazy get
    val rootKey: String,
    val typeConfig: CwtTypeConfig,
    val elementPath: ParadoxElementPath,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
    val element: ParadoxScriptDefinitionElement,
    //element直接作为属性的话可能会有些问题，不过这个缓存会在所在脚本文件变更时被清除，应当问题不大
    //element不能转为SmartPsiElementPointer然后作为属性，这会导致与ParadoxDefinitionMemberSInfo.element引发递归异常
): UserDataHolderBase() {
    enum class SourceType { Default, Stub }
    
    var sourceType: SourceType = SourceType.Default
    
    val type: String = typeConfig.name
    
    //NOTE 部分属性需要使用懒加载
    
    val name: String by lazy {
        //NOTE 这里不处理内联的情况
        if(name0 != null) return@lazy name0
        
        //name_from_file = yes -> 返回不包含扩展名的文件名，即rootKey
        val nameFromFileConfig = typeConfig.nameFromFile
        if(nameFromFileConfig) return@lazy rootKey
        //name_field = xxx -> 返回对应名字（xxx）的property的stringValue，如果不存在则为匿名
        val nameField = typeConfig.nameField
        if(nameField != null) {
            val nameProperty = element.findProperty(nameField)
            return@lazy nameProperty?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
        }
        //否则直接返回rootKey
        rootKey
    }
    
    val subtypes: List<String> by lazy {
        subtypeConfigs.map { it.name }
    }
    
    val subtypeConfigs: List<CwtSubtypeConfig> by lazy { getSubtypeConfigs() }
    
    val types: List<String> by lazy {
        mutableListOf(type).apply { addAll(subtypes) }
    }
    
    val typesText: String by lazy {
        types.joinToString(", ")
    }
    
    val declaration: CwtPropertyConfig? by lazy { getDeclaration() }
    
    val localisations: List<ParadoxDefinitionRelatedLocalisationInfo> by lazy {
        val mergedLocalisationConfig = typeConfig.localisation?.getMergedConfigs(subtypes) ?: return@lazy emptyList()
        val result = SmartList<ParadoxDefinitionRelatedLocalisationInfo>()
        //从已有的cwt规则
        for(config in mergedLocalisationConfig) {
            val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
            val info = ParadoxDefinitionRelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        result
    }
    
    val images: List<ParadoxDefinitionRelatedImageInfo> by lazy {
        val mergedImagesConfig = typeConfig.images?.getMergedConfigs(subtypes) ?: return@lazy emptyList()
        val result = SmartList<ParadoxDefinitionRelatedImageInfo>()
        //从已有的cwt规则
        for(config in mergedImagesConfig) {
            val locationExpression = CwtImageLocationExpression.resolve(config.value)
            val info = ParadoxDefinitionRelatedImageInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        result
    }
    
    val modifiers: List<ParadoxDefinitionModifierInfo> by lazy {
        buildList {
            configGroup.typeToModifiersMap.get(type)?.forEach { (_, v) -> add(ParadoxDefinitionModifierInfo(v.template.extract(name), v)) }
            for(subtype in subtypes) {
                configGroup.typeToModifiersMap.get("$type.$subtype")?.forEach { (_, v) -> add(ParadoxDefinitionModifierInfo(v.template.extract(name), v)) }
            }
        }
    }
    
    val primaryLocalisations: List<ParadoxDefinitionRelatedLocalisationInfo> by lazy {
        localisations.filter { it.primary || it.inferIsPrimary() }
    }
    
    val primaryImages: List<ParadoxDefinitionRelatedImageInfo> by lazy {
        images.filter { it.primary || it.inferIsPrimary() }
    }
    
    val localisationConfig get() = typeConfig.localisation
    
    val imagesConfig get() = typeConfig.images
    
    val declarationConfig get() = configGroup.declarations.get(type)
    
    val project get() = configGroup.project
    
    
    fun getSubtypeConfigs(matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtSubtypeConfig> {
        return subtypeConfigsCache.getOrPut(matchOptions) { doGetSubtypeConfigs(matchOptions) }
    }
    
    private val subtypeConfigsCache = ConcurrentHashMap<Int, List<CwtSubtypeConfig>>()
    
    private fun doGetSubtypeConfigs(matchOptions: Int): List<CwtSubtypeConfig> {
        val subtypesConfig = typeConfig.subtypes
        val result = SmartList<CwtSubtypeConfig>()
        for(subtypeConfig in subtypesConfig.values) {
            if(ParadoxDefinitionHandler.matchesSubtype(element, subtypeConfig, rootKey, configGroup, result, matchOptions)) {
                result.add(subtypeConfig)
            }
        }
        return result
    }
    
    fun getDeclaration(matchOptions: Int = ParadoxConfigMatcher.Options.Default): CwtPropertyConfig? {
        return declarationConfigsCache.getOrPut(matchOptions) { doGetDeclaration(matchOptions) }
    }
    
    private val declarationConfigsCache = ConcurrentHashMap<Int, CwtPropertyConfig?>()
    
    private fun doGetDeclaration(matchOptions: Int): CwtPropertyConfig? {
        val subtypes = getSubtypeConfigs(matchOptions).map { it.name }
        val configContext = CwtConfigContext(element, name, type, subtypes, configGroup, matchOptions)
        return configGroup.declarations.get(type)?.getMergedConfig(configContext)
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionInfo
            && name == other.name && typesText == other.typesText && gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(name, typesText, gameType)
    }
    
    object Keys
}

@InferApi
private fun ParadoxDefinitionRelatedLocalisationInfo.inferIsPrimary(): Boolean {
    return name.equals("name", true) || name.equals("title", true)
}

@InferApi
private fun ParadoxDefinitionRelatedImageInfo.inferIsPrimary(): Boolean {
    return name.equals("icon", true)
}

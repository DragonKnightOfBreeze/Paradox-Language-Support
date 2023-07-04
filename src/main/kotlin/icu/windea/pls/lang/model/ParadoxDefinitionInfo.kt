package icu.windea.pls.lang.model

import com.intellij.openapi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*
import java.util.concurrent.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * @property elementPath 相对于所属文件的定义成员路径。
 * @property name 定义的名字。如果是空字符串，则表示定义是匿名的。（注意：不一定与定义的顶级键名相同，例如，可能来自某个属性的值）
 * @property rootKey 定义的顶级键名。（注意：不一定是定义的名字）
 */
class ParadoxDefinitionInfo(
    name0: String?, // null -> lazy get
    typeConfig0: CwtTypeConfig,
    subtypeConfigs0: List<CwtSubtypeConfig>?, //null -> lazy get
    val rootKey: String,
    val elementPath: ParadoxElementPath,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
    val element: ParadoxScriptDefinitionElement,
    //element直接作为属性的话可能会有些问题，不过这个缓存会在所在脚本文件变更时被清除，应当问题不大
    //element不能转为SmartPsiElementPointer然后作为属性，这会导致与ParadoxDefinitionMemberInfo.element引发递归异常
) : UserDataHolderBase() {
    //NOTE 部分属性需要使用懒加载
    
    val name: String by lazy {
        //NOTE 这里不处理需要内联的情况
        if(name0 != null) return@lazy name0
        
        //name_from_file = yes -> 返回不包含扩展名的文件名，即rootKey
        val nameFromFileConfig = typeConfig0.nameFromFile
        if(nameFromFileConfig) return@lazy rootKey
        //name_field = xxx -> 返回对应名字（xxx）的property的stringValue，如果不存在则为匿名
        val nameField = typeConfig0.nameField
        if(nameField != null) {
            val nameProperty = element.findProperty(nameField)
            return@lazy nameProperty?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
        }
        //直接返回rootKey
        rootKey
    }
    
    val type: String = typeConfig0.name
    
    val typeConfig: CwtTypeConfig by lazy { typeConfig0 }
    
    val subtypes: List<String> by lazy { subtypeConfigs.map { it.name } }
    
    val subtypeConfigs: List<CwtSubtypeConfig> by lazy { subtypeConfigs0 ?: getSubtypeConfigs() }
    
    val types: List<String> by lazy { mutableListOf(type).apply { addAll(subtypes) } }
    
    val typesText: String by lazy { types.joinToString(", ") }
    
    val declaration: CwtPropertyConfig? by lazy { getDeclaration() }
    
    val localisations: List<ParadoxDefinitionRelatedLocalisationInfo> by lazy {
        val mergedConfig = typeConfig.localisation?.getConfigs(subtypes) ?: return@lazy emptyList()
        val result = mutableListOf<ParadoxDefinitionRelatedLocalisationInfo>()
        //从已有的cwt规则
        for(config in mergedConfig) {
            val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
            val info = ParadoxDefinitionRelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        result
    }
    
    val images: List<ParadoxDefinitionRelatedImageInfo> by lazy {
        val mergedConfig = typeConfig.images?.getConfigs(subtypes) ?: return@lazy emptyList()
        val result = mutableListOf<ParadoxDefinitionRelatedImageInfo>()
        //从已有的cwt规则
        for(config in mergedConfig) {
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
        localisations.filter { it.primary || it.primaryByInference }
    }
    
    val primaryImages: List<ParadoxDefinitionRelatedImageInfo> by lazy {
        images.filter { it.primary || it.primaryByInference }
    }
    
    val localisationConfig get() = typeConfig.localisation
    
    val imagesConfig get() = typeConfig.images
    
    val declarationConfig get() = configGroup.declarations.get(type)
    
    val project get() = configGroup.project
    
    fun getSubtypeConfigs(matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtSubtypeConfig> {
        return subtypeConfigsCache.computeIfAbsent(matchOptions) { doGetSubtypeConfigs(matchOptions) }
    }
    
    private val subtypeConfigsCache = ConcurrentHashMap<Int, List<CwtSubtypeConfig>>()
    
    private fun doGetSubtypeConfigs(matchOptions: Int): List<CwtSubtypeConfig> {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for(subtypeConfig in subtypesConfig.values) {
            if(ParadoxDefinitionHandler.matchesSubtype(element, rootKey, subtypeConfig, result, configGroup, matchOptions)) {
                result.add(subtypeConfig)
            }
        }
        return result
    }
    
    fun getDeclaration(matchOptions: Int = ParadoxConfigMatcher.Options.Default): CwtPropertyConfig? {
        return declarationConfigsCache.computeIfAbsent(matchOptions) { doGetDeclaration(matchOptions) }
    }
    
    private val declarationConfigsCache = ConcurrentHashMap<Int, CwtPropertyConfig?>()
    
    private fun doGetDeclaration(matchOptions: Int): CwtPropertyConfig? {
        val subtypes = getSubtypeConfigs(matchOptions).map { it.name }
        val configContext = CwtDeclarationConfigContext(element, name, type, subtypes, configGroup, matchOptions)
        return configGroup.declarations.get(type)?.getConfig(configContext)
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


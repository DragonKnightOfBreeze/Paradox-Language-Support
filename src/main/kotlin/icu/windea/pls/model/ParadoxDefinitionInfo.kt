package icu.windea.pls.model

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.util.*
import java.util.concurrent.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * @property name 定义的名字。如果是空字符串，则表示定义是匿名的。（注意：不一定与定义的顶级键名相同，例如，可能来自某个属性的值）
 * @property rootKey 定义的顶级键名。（注意：不一定是定义的名字）
 * @property elementPath 相对于所属文件的定义成员路径。
 */
class ParadoxDefinitionInfo(
    val element: ParadoxScriptDefinitionElement, //use element directly here
    name0: String?, // null -> lazy get
    typeConfig0: CwtTypeConfig,
    subtypeConfigs0: List<CwtSubtypeConfig>?, //null -> lazy get
    val rootKey: String,
    val elementPath: ParadoxExpressionPath,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    //NOTE 部分属性需要使用懒加载

    val name: String by lazy {
        //NOTE 这里不处理需要内联的情况

        //name_from_file = yes -> 返回不包含扩展名的文件名，即rootKey
        //name_field = xxx -> 返回对应名字（xxx）的property的stringValue，如果不存在则为匿名

        when {
            name0 != null -> name0
            typeConfig0.nameFromFile -> rootKey
            typeConfig0.nameField == null -> rootKey
            typeConfig0.nameField == "" -> ""
            typeConfig0.nameField == "-" -> element.castOrNull<ParadoxScriptProperty>()?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
            else -> element.findProperty(typeConfig0.nameField)?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
        }
    }

    val type: String = typeConfig0.name

    val typeConfig: CwtTypeConfig by lazy { typeConfig0 }

    val subtypes: List<String> by lazy { subtypeConfigs.map { it.name } }

    val subtypeConfigs: List<CwtSubtypeConfig> by lazy { subtypeConfigs0 ?: getSubtypeConfigs() }

    val types: List<String> by lazy { mutableListOf(type).apply { addAll(subtypes) } }

    val typesText: String by lazy { types.joinToString(", ") }

    val declaration: CwtPropertyConfig? by lazy { getDeclaration() }

    val localisations: List<RelatedLocalisationInfo> by lazy {
        val mergedConfig = typeConfig.localisation?.getConfigs(subtypes) ?: return@lazy emptyList()
        val result = mutableListOf<RelatedLocalisationInfo>()
        //从已有的cwt规则
        for (config in mergedConfig) {
            val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
            val info = RelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        result
    }

    val images: List<RelatedImageInfo> by lazy {
        val mergedConfig = typeConfig.images?.getConfigs(subtypes) ?: return@lazy emptyList()
        val result = mutableListOf<RelatedImageInfo>()
        //从已有的cwt规则
        for (config in mergedConfig) {
            val locationExpression = CwtImageLocationExpression.resolve(config.value)
            val info = RelatedImageInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        result
    }

    val modifiers: List<ModifierInfo> by lazy {
        buildList {
            configGroup.type2ModifiersMap.get(type)?.forEach { (_, v) -> add(ModifierInfo(CwtTemplateExpressionManager.extract(v.template, name), v)) }
            for (subtype in subtypes) {
                configGroup.type2ModifiersMap.get("$type.$subtype")?.forEach { (_, v) -> add(ModifierInfo(CwtTemplateExpressionManager.extract(v.template, name), v)) }
            }
        }
    }

    val primaryLocalisations: List<RelatedLocalisationInfo> by lazy {
        localisations.filter { it.primary || it.primaryByInference }
    }

    val primaryImages: List<RelatedImageInfo> by lazy {
        images.filter { it.primary || it.primaryByInference }
    }

    val localisationConfig get() = typeConfig.localisation

    val imagesConfig get() = typeConfig.images

    val declarationConfig get() = configGroup.declarations.get(type)

    val project get() = configGroup.project

    fun getSubtypeConfigs(matchOptions: Int = ParadoxExpressionMatcher.Options.Default): List<CwtSubtypeConfig> {
        return subtypeConfigsCache.getOrPut(matchOptions) { doGetSubtypeConfigs(matchOptions) }
    }

    private val subtypeConfigsCache = ConcurrentHashMap<Int, List<CwtSubtypeConfig>>()

    private fun doGetSubtypeConfigs(matchOptions: Int): List<CwtSubtypeConfig> {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (ParadoxDefinitionManager.matchesSubtype(element, rootKey, subtypeConfig, result, configGroup, matchOptions)) {
                result.add(subtypeConfig)
            }
        }
        return result
    }

    fun getDeclaration(matchOptions: Int = ParadoxExpressionMatcher.Options.Default): CwtPropertyConfig? {
        return declarationConfigsCache.getOrPut(matchOptions) { doGetDeclaration(matchOptions) }
    }

    private val declarationConfigsCache = ConcurrentHashMap<Int, CwtPropertyConfig?>()

    private fun doGetDeclaration(matchOptions: Int): CwtPropertyConfig? {
        val declarationConfig = configGroup.declarations.get(type) ?: return null
        val subtypes = getSubtypeConfigs(matchOptions).map { it.name }
        val configContext = CwtDeclarationConfigContextProvider.getContext(element, name, type, subtypes, gameType, configGroup)
        return configContext?.getConfig(declarationConfig)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionInfo
            && name == other.name && typesText == other.typesText && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, typesText, gameType)
    }

    object Keys : KeyRegistry()

    data class RelatedImageInfo(
        val key: String,
        val locationExpression: CwtImageLocationExpression,
        val required: Boolean = false,
        val primary: Boolean = false
    ) {
        @Inferred
        val primaryByInference: Boolean = key.equals("icon", true)
    }

    data class RelatedLocalisationInfo(
        val key: String,
        val locationExpression: CwtLocalisationLocationExpression,
        val required: Boolean = false,
        val primary: Boolean = false
    ) {
        @Inferred
        val primaryByInference: Boolean = key.equals("name", true) || key.equals("title", true)
    }

    data class ModifierInfo(
        val name: String,
        val config: CwtModifierConfig
    )
}


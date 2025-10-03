package icu.windea.pls.model

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.type2ModifiersMap
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @property name 定义的名字。如果是空字符串，则表示定义是匿名的。
 * @property typeKey 定义的类型键（不一定是定义的名字）。
 * @property elementPath 相对于所属文件的定义成员路径。
 */
class ParadoxDefinitionInfo(
    val element: ParadoxScriptDefinitionElement, // use element directly here
    val typeConfig: CwtTypeConfig,
    name0: String?, // null -> lazy get
    subtypeConfigs0: List<CwtSubtypeConfig>?, // null -> lazy get
    val typeKey: String,
    val elementPath: ParadoxElementPath,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    // NOTE 部分属性需要使用懒加载

    // NOTE 这里不处理需要内联的情况
    val name: String by lazy { name0 ?: ParadoxDefinitionManager.resolveNameFromTypeConfig(element, typeKey, typeConfig) }

    val subtypeConfigs: List<CwtSubtypeConfig> by lazy { subtypeConfigs0 ?: getSubtypeConfigs() }

    val type: String = typeConfig.name

    val subtypes: List<String> by lazy { subtypeConfigs.map { it.name } }

    val types: List<String> by lazy { mutableListOf(type).apply { addAll(subtypes) } }

    val typesText: String by lazy { types.joinToString(", ") }

    val declaration: CwtPropertyConfig? by lazy { getDeclaration() }

    val localisations: List<RelatedLocalisationInfo> by lazy { doGetLocalisations() }

    val images: List<RelatedImageInfo> by lazy { doGetImages() }

    val modifiers: List<ModifierInfo> by lazy { doGetModifiers() }

    val primaryLocalisations: List<RelatedLocalisationInfo> by lazy { localisations.filter { it.primary || it.primaryByInference }.optimized() }

    val primaryImages: List<RelatedImageInfo> by lazy { images.filter { it.primary || it.primaryByInference }.optimized() }

    val declarationConfig get() = configGroup.declarations.get(type)

    val project get() = configGroup.project

    fun getDeclaration(matchOptions: Int = ParadoxExpressionMatcher.Options.Default): CwtPropertyConfig? {
        return doGetDeclarationFromCache(matchOptions)
    }

    fun getSubtypeConfigs(matchOptions: Int = ParadoxExpressionMatcher.Options.Default): List<CwtSubtypeConfig> {
        return doGetSubtypeConfigsFromCache(matchOptions)
    }

    private val subtypeConfigsCache = ConcurrentHashMap<Int, List<CwtSubtypeConfig>>()

    private fun doGetSubtypeConfigsFromCache(matchOptions: Int): List<CwtSubtypeConfig> {
        return subtypeConfigsCache.getOrPut(matchOptions) { doGetSubtypeConfigs(matchOptions) }
    }

    private fun doGetSubtypeConfigs(matchOptions: Int): List<CwtSubtypeConfig> {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (ParadoxDefinitionManager.matchesSubtype(element, typeKey, subtypeConfig, result, configGroup, matchOptions)) {
                result.add(subtypeConfig)
            }
        }
        return result.optimized()
    }

    private val declarationConfigsCache = ConcurrentHashMap<Int, Any>()

    private fun doGetDeclarationFromCache(matchOptions: Int): CwtPropertyConfig? {
        return declarationConfigsCache.getOrPut(matchOptions) { doGetDeclaration(matchOptions) ?: EMPTY_OBJECT }.castOrNull()
    }

    private fun doGetDeclaration(matchOptions: Int): CwtPropertyConfig? {
        val declarationConfig = configGroup.declarations.get(type) ?: return null
        val subtypes = getSubtypeConfigs(matchOptions).map { it.name }
        val declarationConfigContext = CwtDeclarationConfigContextProvider.getContext(element, name, type, subtypes, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    private fun doGetLocalisations(): List<RelatedLocalisationInfo> {
        val mergedConfig = typeConfig.localisation?.getConfigs(subtypes) ?: return emptyList()
        val result = mutableListOf<RelatedLocalisationInfo>()
        // 从已有的cwt规则
        for (config in mergedConfig) {
            val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
            val info = RelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        return result.optimized()
    }

    private fun doGetImages(): List<RelatedImageInfo> {
        val mergedConfig = typeConfig.images?.getConfigs(subtypes) ?: return emptyList()
        val result = mutableListOf<RelatedImageInfo>()
        // 从已有的cwt规则
        for (config in mergedConfig) {
            val locationExpression = CwtImageLocationExpression.resolve(config.value)
            val info = RelatedImageInfo(config.key, locationExpression, config.required, config.primary)
            result.add(info)
        }
        return result.optimized()
    }

    private fun doGetModifiers(): List<ModifierInfo> {
        return buildList {
            configGroup.type2ModifiersMap.get(type)?.forEach { (_, v) ->
                add(ModifierInfo(CwtTemplateExpressionManager.extract(v.template, name), v))
            }
            for (subtype in subtypes) {
                configGroup.type2ModifiersMap.get("$type.$subtype")?.forEach { (_, v) ->
                    add(ModifierInfo(CwtTemplateExpressionManager.extract(v.template, name), v))
                }
            }
        }.optimized()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionInfo
            && name == other.name && typesText == other.typesText && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, typesText, gameType)
    }

    override fun toString(): String {
        return "ParadoxDefinitionInfo(name=$name, types=$typesText, gameType=$gameType)"
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

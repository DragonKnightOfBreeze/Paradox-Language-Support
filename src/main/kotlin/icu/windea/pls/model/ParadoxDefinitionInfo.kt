package icu.windea.pls.model

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.type2ModifiersMap
import icu.windea.pls.config.util.CwtTemplateExpressionManager
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @property doGetName 定义的名字。如果是空字符串，则表示定义是匿名的。（注意：不一定与定义的顶级键名相同，例如，可能来自某个属性的值）
 * @property rootKey 定义的顶级键名。（注意：不一定是定义的名字）
 * @property elementPath 相对于所属文件的定义成员路径。
 */
class ParadoxDefinitionInfo(
    val element: ParadoxScriptDefinitionElement, //use element directly here
    val typeConfig: CwtTypeConfig,
    name0: String?, // null -> lazy get
    subtypeConfigs0: List<CwtSubtypeConfig>?, //null -> lazy get
    val rootKey: String,
    val elementPath: ParadoxExpressionPath,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    //NOTE 部分属性需要使用懒加载

    val name: String by lazy { name0 ?: doGetName() }

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

    private fun doGetName(): String {
        //NOTE 这里不处理需要内联的情况

        return when {
            //use root key (aka file name without extension), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameFromFile -> rootKey.removePrefix(typeConfig.startsWith.orEmpty())
            //use root key (aka property name), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameField == null -> rootKey.removePrefix(typeConfig.startsWith.orEmpty())
            //force empty (aka anonymous)
            typeConfig.nameField == "" -> ""
            //from property value (which should be a string)
            typeConfig.nameField == "-" -> element.castOrNull<ParadoxScriptProperty>()?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
            //from specific property value in definition declaration (while the property name is declared by config property "name_field")
            else -> element.findProperty(typeConfig.nameField)?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
        }
    }

    private val subtypeConfigsCache = ConcurrentHashMap<Int, List<CwtSubtypeConfig>>()

    private fun doGetSubtypeConfigsFromCache(matchOptions: Int): List<CwtSubtypeConfig> {
        return subtypeConfigsCache.getOrPut(matchOptions) { doGetSubtypeConfigs(matchOptions) }
    }

    private fun doGetSubtypeConfigs(matchOptions: Int): List<CwtSubtypeConfig> {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (ParadoxDefinitionManager.matchesSubtype(element, rootKey, subtypeConfig, result, configGroup, matchOptions)) {
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
        val configContext = CwtDeclarationConfigContextProvider.getContext(element, name, type, subtypes, gameType, configGroup)
        return configContext?.getConfig(declarationConfig)
    }

    private fun doGetLocalisations(): List<RelatedLocalisationInfo> {
        val mergedConfig = typeConfig.localisation?.getConfigs(subtypes) ?: return emptyList()
        val result = mutableListOf<RelatedLocalisationInfo>()
        //从已有的cwt规则
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
        //从已有的cwt规则
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

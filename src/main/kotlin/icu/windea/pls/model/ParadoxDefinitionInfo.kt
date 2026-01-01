package icu.windea.pls.model

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 定义信息。
 *
 * @property doGetName 定义的名字。如果是空字符串，则表示定义是匿名的。
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
) : UserDataHolderBase() {
    val configGroup get() = typeConfig.configGroup
    val gameType get() = configGroup.gameType
    val project get() = configGroup.project
    val declarationConfig get() = configGroup.declarations.get(type)

    private val subtypeConfigsCache = ConcurrentHashMap<Int, List<CwtSubtypeConfig>>()
    private val declarationConfigsCache = ConcurrentHashMap<Int, Any>()

    val name: String by lazy { name0 ?: doGetName() }
    val type: String = typeConfig.name
    val subtypes: List<String> by lazy { doGetSubtypes() }
    val types: List<String> by lazy { doGetTypes() }
    val typesText: String get() = types.joinToString(", ")

    val subtypeConfigs: List<CwtSubtypeConfig> by lazy { subtypeConfigs0 ?: getSubtypeConfigs() }
    val declaration: CwtPropertyConfig? by lazy { getDeclaration() }

    val localisations: List<RelatedLocalisationInfo> by lazy { doGetLocalisations() }
    val images: List<RelatedImageInfo> by lazy { doGetImages() }
    val modifiers: List<ModifierInfo> by lazy { doGetModifiers() }
    val primaryLocalisations: List<RelatedLocalisationInfo> by lazy { doGetPrimaryLocalisations() }
    val primaryImages: List<RelatedImageInfo> by lazy { doGetPrimaryImages() }

    fun getSubtypeConfigs(matchOptions: Int = ParadoxMatchOptions.Default): List<CwtSubtypeConfig> {
        return doGetSubtypeConfigs(matchOptions)
    }

    fun getDeclaration(matchOptions: Int = ParadoxMatchOptions.Default): CwtPropertyConfig? {
        return doGetDeclaration(matchOptions)
    }

    private fun doGetName(): String {
        return ParadoxDefinitionService.resolveName(element, typeKey, typeConfig)
    }

    private fun doGetSubtypes(): List<String> {
        val result = subtypeConfigs.map { it.name }
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetTypes(): List<String> {
        val result = buildList(subtypes.size + 1) { add(type); addAll(subtypes) }
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetSubtypeConfigs(matchOptions: Int): List<CwtSubtypeConfig> {
        if (typeConfig.subtypes.isEmpty()) return emptyList()
        val cache = subtypeConfigsCache
        val result = cache.getOrPut(matchOptions) {
            ParadoxDefinitionService.resolveSubtypeConfigs(this, matchOptions)
        }
        return result.optimized()
    }

    private fun doGetDeclaration(matchOptions: Int): CwtPropertyConfig? {
        val cache = declarationConfigsCache
        val result = cache.getOrPut(matchOptions) {
            ParadoxDefinitionService.resolveDeclaration(this, matchOptions) ?: EMPTY_OBJECT
        }
        return result.castOrNull()
    }

    private fun doGetLocalisations(): List<RelatedLocalisationInfo> {
        val result = ParadoxDefinitionService.resolveRelatedLocalisations(this)
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetImages(): List<RelatedImageInfo> {
        val result = ParadoxDefinitionService.resolveRelatedImages(this)
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetModifiers(): List<ModifierInfo> {
        val result = ParadoxDefinitionService.resolveModifiers(this)
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetPrimaryLocalisations(): List<RelatedLocalisationInfo> {
        val result = localisations.filter { it.primary || it.primaryByInference }
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetPrimaryImages(): List<RelatedImageInfo> {
        val result = images.filter { it.primary || it.primaryByInference }
        return result.optimized() // optimized to optimize memory
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

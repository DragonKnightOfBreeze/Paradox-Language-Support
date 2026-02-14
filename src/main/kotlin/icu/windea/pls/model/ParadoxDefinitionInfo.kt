package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import java.util.*

/**
 * 定义的解析信息。
 *
 * @property source 来源。
 * @property name 名字。如果是空字符串，则表示此定义是匿名的。
 * @property type 类型。
 * @property typeKey 类型键（不一定是定义的名字）。
 * @property rootKeys 一组顶级键。
 * @property typeConfig 对应的类型规则。
 * @property memberPath 成员路径。作为文件的定义的成员路径始终为空。
 */
class ParadoxDefinitionInfo(
    val element: ParadoxDefinitionElement, // use element directly here
    val source: ParadoxDefinitionSource,
    val typeConfig: CwtTypeConfig,
    val name: String,
    val typeKey: String,
    val rootKeys: List<String>,
) : UserDataHolderBase() {
    val configGroup: CwtConfigGroup get() = typeConfig.configGroup
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
    val declarationConfig: CwtDeclarationConfig? get() = configGroup.declarations.get(type)

    val type: String = typeConfig.name

    val subtypes: List<String> get() = ParadoxConfigManager.getSubtypes(subtypeConfigs)
    val types: List<String> get() = ParadoxConfigManager.getTypes(type, subtypeConfigs)
    val typeText: String get() = ParadoxConfigManager.getTypeText(type, subtypeConfigs)

    val memberPath: ParadoxMemberPath = doGetMemberPath()

    val subtypeConfigs: List<CwtSubtypeConfig> get() = getSubtypeConfigs()
    val declaration: CwtPropertyConfig? get() = getDeclaration()

    val localisations: List<RelatedLocalisationInfo> by lazy { doGetLocalisations() }
    val images: List<RelatedImageInfo> by lazy { doGetImages() }
    val modifiers: List<ModifierInfo> by lazy { doGetModifiers() }
    val primaryLocalisations: List<RelatedLocalisationInfo> by lazy { doGetPrimaryLocalisations() }
    val primaryImages: List<RelatedImageInfo> by lazy { doGetPrimaryImages() }

    fun getSubtypeConfigs(options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        return ParadoxDefinitionManager.getSubtypeConfigs(this, options)
    }

    fun getDeclaration(options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        return ParadoxDefinitionManager.getDeclaration(this, options)
    }

    private fun doGetMemberPath(): ParadoxMemberPath {
        // NOTE 2.1.2 file definition has empty member path
        if (typeConfig.typePerFile/* || element is ParadoxScriptFile*/) return ParadoxMemberPath.resolveEmpty()

        return ParadoxMemberPath.resolve(rootKeys + typeKey).normalize()
    }

    private fun doGetLocalisations(): List<RelatedLocalisationInfo> {
        val result = ParadoxDefinitionService.resolveRelatedLocalisationInfos(this)
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetImages(): List<RelatedImageInfo> {
        val result = ParadoxDefinitionService.resolveRelatedImageInfos(this)
        return result.optimized() // optimized to optimize memory
    }

    private fun doGetModifiers(): List<ModifierInfo> {
        val result = ParadoxDefinitionService.resolveModifierInfos(this)
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
            && name == other.name && type == other.type && typeKey == other.typeKey && rootKeys == other.rootKeys && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, type, typeKey, rootKeys, gameType)
    }

    override fun toString(): String {
        return "ParadoxDefinitionInfo(source=$source, name=$name, type=$type, typeKey=$typeKey, rootKeys=$rootKeys, gameType=$gameType)"
    }

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

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
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement

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
data class ParadoxDefinitionInfo(
    val source: ParadoxDefinitionSource,
    val name: String,
    val type: String,
    val typeKey: String,
    val rootKeys: List<String>,
    val typeConfig: CwtTypeConfig,
) : UserDataHolderBase() {
    val memberPath: ParadoxMemberPath = ParadoxDefinitionManager.getMemberPath(this)

    @Volatile var element: ParadoxDefinitionElement? = null

    val configGroup: CwtConfigGroup get() = typeConfig.configGroup
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
    val declarationConfig: CwtDeclarationConfig? get() = configGroup.declarations.get(type)

    val subtypes: List<String> get() = ParadoxConfigManager.getSubtypes(subtypeConfigs)
    val types: List<String> get() = ParadoxConfigManager.getTypes(type, subtypeConfigs)
    val typeText: String get() = ParadoxConfigManager.getTypeText(type, subtypeConfigs)

    val subtypeConfigs: List<CwtSubtypeConfig> get() = getSubtypeConfigs()
    val declaration: CwtPropertyConfig? get() = getDeclaration()

    // NOTE 2.1.3 以下属性目前保持为计算属性即可，不需要额外缓存
    val localisations: List<RelatedLocalisationInfo> get() = ParadoxDefinitionManager.getRelatedLocalisationInfos(this)
    val images: List<RelatedImageInfo> get() = ParadoxDefinitionManager.getRelatedImageInfos(this)
    val modifiers: List<ModifierInfo> get() = ParadoxDefinitionManager.getModifierInfos(this)
    val primaryLocalisations: List<RelatedLocalisationInfo> get() = ParadoxDefinitionManager.getPrimaryRelatedLocalisationInfos(this)
    val primaryImages: List<RelatedImageInfo> get() = ParadoxDefinitionManager.getPrimaryRelatedImageInfos(this)

    /** @see ParadoxDefinitionManager.getSubtypeConfigs */
    fun getSubtypeConfigs(options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> = ParadoxDefinitionManager.getSubtypeConfigs(this, options)

    /** @see ParadoxDefinitionManager.getDeclaration */
    fun getDeclaration(options: ParadoxMatchOptions? = null): CwtPropertyConfig? = ParadoxDefinitionManager.getDeclaration(this, options)

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

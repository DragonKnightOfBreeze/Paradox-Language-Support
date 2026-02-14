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
    val typeConfig: CwtTypeConfig,
    val name: String,
    val typeKey: String,
    val rootKeys: List<String>,
) : UserDataHolderBase() {
    @Volatile var element: ParadoxDefinitionElement? = null

    val configGroup: CwtConfigGroup get() = typeConfig.configGroup
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
    val declarationConfig: CwtDeclarationConfig? get() = configGroup.declarations.get(type)

    val type: String = typeConfig.name

    val subtypes: List<String> get() = ParadoxConfigManager.getSubtypes(subtypeConfigs)
    val types: List<String> get() = ParadoxConfigManager.getTypes(type, subtypeConfigs)
    val typeText: String get() = ParadoxConfigManager.getTypeText(type, subtypeConfigs)

    val memberPath: ParadoxMemberPath get() = ParadoxDefinitionManager.getMemberPath(this)

    val subtypeConfigs: List<CwtSubtypeConfig> get() = getSubtypeConfigs()
    val declaration: CwtPropertyConfig? get() = getDeclaration()

    val localisations: List<RelatedLocalisationInfo> by lazy { ParadoxDefinitionManager.getRelatedLocalisationInfos(this) }
    val images: List<RelatedImageInfo> by lazy { ParadoxDefinitionManager.getRelatedImageInfos(this) }
    val modifiers: List<ModifierInfo> by lazy { ParadoxDefinitionManager.getModifierInfos(this) }
    val primaryLocalisations: List<RelatedLocalisationInfo> by lazy { ParadoxDefinitionManager.getPrimaryRelatedLocalisationInfos(this) }
    val primaryImages: List<RelatedImageInfo> by lazy { ParadoxDefinitionManager.getPrimaryRelatedImageInfos(this) }

    fun getSubtypeConfigs(options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        return ParadoxDefinitionManager.getSubtypeConfigs(this, options)
    }

    fun getDeclaration(options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        return ParadoxDefinitionManager.getDeclaration(this, options)
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

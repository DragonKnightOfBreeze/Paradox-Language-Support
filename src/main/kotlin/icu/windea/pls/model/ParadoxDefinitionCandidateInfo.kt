package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.util.ParadoxConfigManager

/**
 * 定义候选的解析信息。
 *
 * 可能对应一个（非注入的）定义，也可能对应一个定义注入。
 *
 * @property type 类型。
 * @property typeConfig 对应的类型规则。
 * @property configGroup 对应的规则分组。
 */
sealed interface ParadoxDefinitionCandidateInfo : UserDataHolder {
    val source: ParadoxDefinitionSource
    val type: String?
    val typeConfig: CwtTypeConfig?
    val configGroup: CwtConfigGroup

    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
    val declarationConfig: CwtDeclarationConfig? get() = configGroup.declarations.get(type)

    val subtypes: List<String> get() = ParadoxConfigManager.getSubtypes(subtypeConfigs)
    val types: List<String> get() = ParadoxConfigManager.getTypes(type, subtypeConfigs)
    val typeText: String get() = ParadoxConfigManager.getTypeText(type, subtypeConfigs)

    val subtypeConfigs: List<CwtSubtypeConfig> get() = getSubtypeConfigs()
    val declaration: CwtPropertyConfig? get() = getDeclaration()

    fun getSubtypeConfigs(options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig>

    fun getDeclaration(options: ParadoxMatchOptions? = null): CwtPropertyConfig?
}

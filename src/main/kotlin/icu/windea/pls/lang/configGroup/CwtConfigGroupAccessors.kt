package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.annotations.*

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class Tags(vararg val value: Tag)

enum class Tag {
    Computed, Extended
}

@Tags(Tag.Extended)
val CwtConfigGroup.foldingSettings: Map<@CaseInsensitive String, Map<String, CwtFoldingSetting>>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
@Tags(Tag.Extended)
val CwtConfigGroup.postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSetting>>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }

@Tags(Tag.Extended)
val CwtConfigGroup.systemLinks: Map<@CaseInsensitive String, CwtSystemLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
@Tags(Tag.Extended)
val CwtConfigGroup.localisationLocalesById: Map<String, CwtLocalisationLocaleConfig>
    by createKeyDelegate { mutableMapOf() }
@Tags(Tag.Extended)
val CwtConfigGroup.localisationLocalesByCode: Map<String, CwtLocalisationLocaleConfig>
    by createKeyDelegate { mutableMapOf() }
@Tags(Tag.Extended)
val CwtConfigGroup.localisationPredefinedParameters: Map<String, CwtLocalisationPredefinedParameterConfig>
    by createKeyDelegate { mutableMapOf() }

//unused
val CwtConfigGroup.folders: Set<String>
    by createKeyDelegate { mutableSetOf() }

//type- typeConfig
val CwtConfigGroup.types: Map<String, CwtTypeConfig>
    by createKeyDelegate { mutableMapOf() }
//type - typeConfig
val CwtConfigGroup.swappedTypes: Map<String, CwtTypeConfig>
    by createKeyDelegate { mutableMapOf() }
//typeExpression - modifierTemplate - modifierConfig
val CwtConfigGroup.type2ModifiersMap: Map<String, Map<String, CwtModifierConfig>>
    by createKeyDelegate { mutableMapOf() }

//type - declarationConfig
val CwtConfigGroup.declarations: Map<String, CwtDeclarationConfig>
    by createKeyDelegate { mutableMapOf() }

val CwtConfigGroup.values: Map<String, CwtEnumConfig>
    by createKeyDelegate { mutableMapOf() }
//enumValue可以是int、float、bool类型，统一用字符串表示
val CwtConfigGroup.enums: Map<String, CwtEnumConfig>
    by createKeyDelegate { mutableMapOf() }
//基于enum_name进行定位，对应的可能是key/value
val CwtConfigGroup.complexEnums: Map<String, CwtComplexEnumConfig>
    by createKeyDelegate { mutableMapOf() }

val CwtConfigGroup.links: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.linksAsScopeNotData: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.linksAsScopeWithPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.linksAsScopeWithoutPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.linksAsValueNotData: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.linksAsValueWithPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.linksAsValueWithoutPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.localisationLinks: Map<@CaseInsensitive String, CwtLocalisationLinkConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.localisationCommands: Map<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.scopes: Map<@CaseInsensitive String, CwtScopeConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.scopeGroups: Map<@CaseInsensitive String, CwtScopeGroupConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.singleAliases: Map<String, CwtSingleAliasConfig>
    by createKeyDelegate { mutableMapOf() }
//同名的alias可以有多个
val CwtConfigGroup.aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>>
    by createKeyDelegate { mutableMapOf() }
//inline_script
val CwtConfigGroup.inlineConfigGroup: Map<String, List<CwtInlineConfig>>
    by createKeyDelegate { mutableMapOf() }

// key
val CwtConfigGroup.gameRules: Map<String, CwtGameRuleConfig>
    by createKeyDelegate { mutableMapOf() }
// key
val CwtConfigGroup.onActions: Map<String, CwtOnActionConfig>
    by createKeyDelegate { mutableMapOf() }

val CwtConfigGroup.modifierCategories: Map<String, CwtModifierCategoryConfig>
    by createKeyDelegate { mutableMapOf() }
// key
val CwtConfigGroup.modifiers: Map<@CaseInsensitive String, CwtModifierConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }

//常量字符串的别名的组名的映射
val CwtConfigGroup.aliasKeysGroupConst: Map<@CaseInsensitive String, Map<String, String>>
    by createKeyDelegate { caseInsensitiveStringKeyMap() }
//非常量字符串的别名的组名的映射
val CwtConfigGroup.aliasKeysGroupNoConst: Map<String, Set<String>>
    by createKeyDelegate { mutableMapOf() }

//处理后的连接规则

val CwtConfigGroup.linksAsScopeWithPrefixSorted: List<CwtLinkConfig>
    by createKeyDelegate { mutableListOf() }
val CwtConfigGroup.linksAsValueWithPrefixSorted: List<CwtLinkConfig>
    by createKeyDelegate { mutableListOf() }
val CwtConfigGroup.linksAsScopeWithoutPrefixSorted: List<CwtLinkConfig>
    by createKeyDelegate { mutableListOf() }
val CwtConfigGroup.linksAsValueWithoutPrefixSorted: List<CwtLinkConfig>
    by createKeyDelegate { mutableListOf() }
val CwtConfigGroup.linksAsVariable: List<CwtLinkConfig>
    by createKeyDelegate { mutableListOf() }

//必定支持作用域的CWT别名规则
@Tags(Tag.Computed)
val CwtConfigGroup.aliasNamesSupportScope: Set<String>
    by createKeyDelegate { mutableSetOf() }
//必定支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSupportScope: Set<String>
    by createKeyDelegate { mutableSetOf() }
//必定间接支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesIndirectSupportScope: Set<String>
    by createKeyDelegate { mutableSetOf() }
//不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSkipCheckSystemLink: Set<String>
    by createKeyDelegate { mutableSetOf() }
//支持参数的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.definitionTypesSupportParameters: Set<String>
    by createKeyDelegate { mutableSetOf() }

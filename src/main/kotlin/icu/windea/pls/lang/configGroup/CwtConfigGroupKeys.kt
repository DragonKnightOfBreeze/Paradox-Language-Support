package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class Tags(vararg val value: Tag)

enum class Tag {
    Computed, Extended
}

@Tags(Tag.Extended)
val CwtConfigGroup.Keys.foldingSettings
    by createKey<Map<String, Map<@CaseInsensitive String, CwtFoldingSetting>>> { caseInsensitiveStringKeyMap() }
@Tags(Tag.Extended)
val CwtConfigGroup.Keys.postfixTemplateSettings
    by createKey<Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSetting>>> { caseInsensitiveStringKeyMap() }

@Tags(Tag.Extended)
val CwtConfigGroup.Keys.systemLinks
    by createKey<Map<@CaseInsensitive String, CwtSystemLinkConfig>> { caseInsensitiveStringKeyMap() }
@Tags(Tag.Extended)
val CwtConfigGroup.Keys.localisationLocalesById
    by createKey<Map<String, CwtLocalisationLocaleConfig>> { mutableMapOf() }
@Tags(Tag.Extended)
val CwtConfigGroup.Keys.localisationLocalesByCode
    by createKey<Map<String, CwtLocalisationLocaleConfig>> { mutableMapOf() }
@Tags(Tag.Extended)
val CwtConfigGroup.Keys.localisationPredefinedParameters
    by createKey<Map<String, CwtLocalisationPredefinedParameterConfig>> { mutableMapOf() }

//unused
val CwtConfigGroup.Keys.folders
    by createKey<Set<String>> { mutableSetOf() }

//type- typeConfig
val CwtConfigGroup.Keys.types
    by createKey<Map<String, CwtTypeConfig>> { mutableMapOf() }
//type - typeConfig
val CwtConfigGroup.Keys.swappedTypes
    by createKey<Map<String, CwtTypeConfig>> { mutableMapOf() }
//typeExpression - modifierTemplate - modifierConfig
val CwtConfigGroup.Keys.type2ModifiersMap
    by createKey<Map<String, Map<String, CwtModifierConfig>>> { mutableMapOf() }

//type - declarationConfig
val CwtConfigGroup.Keys.declarations
    by createKey<Map<String, CwtDeclarationConfig>> { mutableMapOf() }

val CwtConfigGroup.Keys.values
    by createKey<Map<String, CwtEnumConfig>> { mutableMapOf() }
//enumValue可以是int、float、bool类型，统一用字符串表示
val CwtConfigGroup.Keys.enums
    by createKey<Map<String, CwtEnumConfig>> { mutableMapOf() }
//基于enum_name进行定位，对应的可能是key/value
val CwtConfigGroup.Keys.complexEnums
    by createKey<Map<String, CwtComplexEnumConfig>> { mutableMapOf() }

val CwtConfigGroup.Keys.links
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsScopeNotData
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsScopeWithPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsScopeWithoutPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsValueNotData
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsValueWithPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsValueWithoutPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>> { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.localisationLinks
    by createKey<Map<@CaseInsensitive String, CwtLocalisationLinkConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.localisationCommands
    by createKey<Map<@CaseInsensitive String, CwtLocalisationCommandConfig>> { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.scopes
    by createKey<Map<@CaseInsensitive String, CwtScopeConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.scopeAliasMap
    by createKey<Map<@CaseInsensitive String, CwtScopeConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.scopeGroups
    by createKey<Map<@CaseInsensitive String, CwtScopeGroupConfig>> { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.singleAliases
    by createKey<Map<String, CwtSingleAliasConfig>> { mutableMapOf() }
//同名的alias可以有多个
val CwtConfigGroup.Keys.aliasGroups
    by createKey<Map<String, Map<String, List<CwtAliasConfig>>>> { mutableMapOf() }
//inline_script
val CwtConfigGroup.Keys.inlineConfigGroup
    by createKey<Map<String, List<CwtInlineConfig>>> { mutableMapOf() }

// key
val CwtConfigGroup.Keys.gameRules
    by createKey<Map<String, CwtGameRuleConfig>> { mutableMapOf() }
// key
val CwtConfigGroup.Keys.onActions
    by createKey<Map<String, CwtOnActionConfig>> { mutableMapOf() }

val CwtConfigGroup.Keys.modifierCategories
    by createKey<Map<String, CwtModifierCategoryConfig>> { mutableMapOf() }
// key
val CwtConfigGroup.Keys.modifiers
    by createKey<Map<@CaseInsensitive String, CwtModifierConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.predefinedModifiers
    by createKey<Map<@CaseInsensitive String, CwtModifierConfig>> { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.generatedModifiers
    by createKey<Map<@CaseInsensitive String, CwtModifierConfig>> { caseInsensitiveStringKeyMap() }

//常量字符串的别名的组名的映射
val CwtConfigGroup.Keys.aliasKeysGroupConst
    by createKey<Map<String, Map<@CaseInsensitive String, String>>> { caseInsensitiveStringKeyMap() }
//非常量字符串的别名的组名的映射
val CwtConfigGroup.Keys.aliasKeysGroupNoConst
    by createKey<Map<String, Set<String>>> { mutableMapOf() }

//处理后的连接规则

val CwtConfigGroup.Keys.linksAsScopeWithPrefixSorted
    by createKey<List<CwtLinkConfig>> { mutableListOf() }
val CwtConfigGroup.Keys.linksAsValueWithPrefixSorted
    by createKey<List<CwtLinkConfig>> { mutableListOf() }
val CwtConfigGroup.Keys.linksAsScopeWithoutPrefixSorted
    by createKey<List<CwtLinkConfig>> { mutableListOf() }
val CwtConfigGroup.Keys.linksAsValueWithoutPrefixSorted
    by createKey<List<CwtLinkConfig>> { mutableListOf() }
val CwtConfigGroup.Keys.linksAsVariable
    by createKey<List<CwtLinkConfig>> { mutableListOf() }

//必定支持作用域的CWT别名规则
@Tags(Tag.Computed)
val CwtConfigGroup.Keys.aliasNamesSupportScope
    by createKey<Set<String>> { mutableSetOf() }
//必定支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.Keys.definitionTypesSupportScope
    by createKey<Set<String>> { mutableSetOf() }
//必定间接支持作用域的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.Keys.definitionTypesIndirectSupportScope
    by createKey<Set<String>> { mutableSetOf() }
//不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）
@Tags(Tag.Computed)
val CwtConfigGroup.Keys.definitionTypesSkipCheckSystemLink
    by createKey<Set<String>> { mutableSetOf() }
//支持参数的定义类型
@Tags(Tag.Computed)
val CwtConfigGroup.Keys.definitionTypesSupportParameters
    by createKey<Set<String>> { mutableSetOf() }

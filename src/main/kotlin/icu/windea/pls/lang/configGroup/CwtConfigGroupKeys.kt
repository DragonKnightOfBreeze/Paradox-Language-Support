package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*

val CwtConfigGroup.Keys.foldingSettings
    by createKey<Map<String, Map<@CaseInsensitive String, CwtFoldingSetting>>>("foldingSettings") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.postfixTemplateSettings
    by createKey<Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSetting>>>("postfixTemplateSettings") { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.systemLinks
    by createKey<Map<@CaseInsensitive String, CwtSystemLinkConfig>>("systemLinks") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.localisationLocalesById
    by createKey<Map<String, CwtLocalisationLocaleConfig>>("localisationLocalesById") { mutableMapOf() }
val CwtConfigGroup.Keys.localisationLocalesByCode
    by createKey<Map<String, CwtLocalisationLocaleConfig>>("localisationLocalesByCode") { mutableMapOf() }
val CwtConfigGroup.Keys.localisationPredefinedParameters
    by createKey<Map<String, CwtLocalisationPredefinedParameterConfig>>("localisationPredefinedParameters") { mutableMapOf() }

//unused
val CwtConfigGroup.Keys.folders
    by createKey<Set<String>>("folders") { mutableSetOf() }

//type- typeConfig
val CwtConfigGroup.Keys.types
    by createKey<Map<String, CwtTypeConfig>>("types") { mutableMapOf() }
//type - typeConfig
val CwtConfigGroup.Keys.swappedTypes
    by createKey<Map<String, CwtTypeConfig>>("swappedTypes") { mutableMapOf() }
//typeExpression - modifierTemplate - modifierConfig
val CwtConfigGroup.Keys.type2ModifiersMap
    by createKey<Map<String, Map<String, CwtModifierConfig>>>("type2ModifiersMap") { mutableMapOf() }

//type - declarationConfig
val CwtConfigGroup.Keys.declarations
    by createKey<Map<String, CwtDeclarationConfig>>("declarations") { mutableMapOf() }

val CwtConfigGroup.Keys.values
    by createKey<Map<String, CwtEnumConfig>>("values") { mutableMapOf() }
//enumValue可以是int、float、bool类型，统一用字符串表示
val CwtConfigGroup.Keys.enums
    by createKey<Map<String, CwtEnumConfig>>("enums") { mutableMapOf() }
//基于enum_name进行定位，对应的可能是key/value
val CwtConfigGroup.Keys.complexEnums
    by createKey<Map<String, CwtComplexEnumConfig>>("complexEnums") { mutableMapOf() }

val CwtConfigGroup.Keys.links
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("links") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsScopeNotData
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("linksAsScopeNotData") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsScopeWithPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("linksAsScopeWithPrefix") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsScopeWithoutPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("linksAsScopeWithoutPrefix") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsValueNotData
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("linksAsValueNotData") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsValueWithPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("linksAsValueWithPrefix") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.linksAsValueWithoutPrefix
    by createKey<Map<@CaseInsensitive String, CwtLinkConfig>>("linksAsValueWithoutPrefix") { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.localisationLinks
    by createKey<Map<@CaseInsensitive String, CwtLocalisationLinkConfig>>("localisationLinks") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.localisationCommands
    by createKey<Map<@CaseInsensitive String, CwtLocalisationCommandConfig>>("localisationCommands") { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.scopes
    by createKey<Map<@CaseInsensitive String, CwtScopeConfig>>("scopes") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.scopeAliasMap
    by createKey<Map<@CaseInsensitive String, CwtScopeConfig>>("scopeAliasMap") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.scopeGroups
    by createKey<Map<@CaseInsensitive String, CwtScopeGroupConfig>>("scopeGroups") { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.Keys.singleAliases
    by createKey<Map<String, CwtSingleAliasConfig>>("singleAliases") { mutableMapOf() }
//同名的alias可以有多个
val CwtConfigGroup.Keys.aliasGroups
    by createKey<Map<String, Map<String, List<CwtAliasConfig>>>>("aliasGroups") { mutableMapOf() }
//inline_script
val CwtConfigGroup.Keys.inlineConfigGroup
    by createKey<Map<String, List<CwtInlineConfig>>>("inlineConfigGroup") { mutableMapOf() }

// key
val CwtConfigGroup.Keys.gameRules
    by createKey<Map<String, CwtGameRuleConfig>>("gameRules") { mutableMapOf() }
// key
val CwtConfigGroup.Keys.onActions
    by createKey<Map<String, CwtOnActionConfig>>("onActions") { mutableMapOf() }

val CwtConfigGroup.Keys.modifierCategories
    by createKey<Map<String, CwtModifierCategoryConfig>>("modifierCategories") { mutableMapOf() }
// key
val CwtConfigGroup.Keys.modifiers
    by createKey<Map<@CaseInsensitive String, CwtModifierConfig>>("modifiers") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.predefinedModifiers
    by createKey<Map<@CaseInsensitive String, CwtModifierConfig>>("predefinedModifiers") { caseInsensitiveStringKeyMap() }
val CwtConfigGroup.Keys.generatedModifiers
    by createKey<Map<@CaseInsensitive String, CwtModifierConfig>>("generatedModifiers") { caseInsensitiveStringKeyMap() }

//常量字符串的别名的组名的映射
val CwtConfigGroup.Keys.aliasKeysGroupConst
    by createKey<Map<String, Map<@CaseInsensitive String, String>>>("aliasKeysGroupConst") { caseInsensitiveStringKeyMap() }
//非常量字符串的别名的组名的映射
val CwtConfigGroup.Keys.aliasKeysGroupNoConst
    by createKey<Map<String, Set<String>>>("aliasKeysGroupNoConst") { mutableMapOf() }

//处理后的连接规则

val CwtConfigGroup.Keys.linksAsScopeWithPrefixSorted
    by createKey<List<CwtLinkConfig>>("linksAsScopeWithPrefixSorted") { mutableListOf() }
val CwtConfigGroup.Keys.linksAsValueWithPrefixSorted
    by createKey<List<CwtLinkConfig>>("linksAsValueWithPrefixSorted") { mutableListOf() }
val CwtConfigGroup.Keys.linksAsScopeWithoutPrefixSorted
    by createKey<List<CwtLinkConfig>>("linksAsScopeWithoutPrefixSorted") { mutableListOf() }
val CwtConfigGroup.Keys.linksAsValueWithoutPrefixSorted
    by createKey<List<CwtLinkConfig>>("linksAsValueWithoutPrefixSorted") { mutableListOf() }
val CwtConfigGroup.Keys.linksAsVariable
    by createKey<List<CwtLinkConfig>>("linksAsVariable") { mutableListOf() }

//必定支持作用域的CWT别名规则
val CwtConfigGroup.Keys.aliasNamesSupportScope
    by createKey<Set<String>>("aliasNamesSupportScope") { mutableSetOf() }
//必定支持作用域的定义类型
val CwtConfigGroup.Keys.definitionTypesSupportScope
    by createKey<Set<String>>("definitionTypesSupportScope") { mutableSetOf() }
//必定间接支持作用域的定义类型
val CwtConfigGroup.Keys.definitionTypesIndirectSupportScope
    by createKey<Set<String>>("definitionTypesIndirectSupportScope") { mutableSetOf() }
//不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）
val CwtConfigGroup.Keys.definitionTypesSkipCheckSystemLink
    by createKey<Set<String>>("definitionTypesSkipCheckSystemLink") { mutableSetOf() }
//支持参数的定义类型
val CwtConfigGroup.Keys.definitionTypesSupportParameters
    by createKey<Set<String>>("definitionTypesSupportParameters") { mutableSetOf() }

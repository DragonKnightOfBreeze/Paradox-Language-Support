package icu.windea.pls.config.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy

// region Core

val CwtConfigGroup.priorities: MutableMap<String, ParadoxOverrideStrategy>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.systemScopes: MutableMap<@CaseInsensitive String, CwtSystemScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.localisationLocalesById: MutableMap<String, CwtLocaleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.localisationLocalesByCode: MutableMap<String, CwtLocaleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// type - typeConfig
val CwtConfigGroup.types: MutableMap<String, CwtTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// type - typeConfig
val CwtConfigGroup.swappedTypes: MutableMap<String, CwtTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// typeExpression - modifierTemplate - modifierConfig
val CwtConfigGroup.type2ModifiersMap: MutableMap<String, MutableMap<String, CwtModifierConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// type - declarationConfig
val CwtConfigGroup.declarations: MutableMap<String, CwtDeclarationConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.rows: MutableMap<String, CwtRowConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// enumValue可以是 int、float、bool 类型，统一用字符串表示
val CwtConfigGroup.enums: MutableMap<String, CwtEnumConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// 基于 enum_name 进行定位，对应的可能是 key/value
val CwtConfigGroup.complexEnums: MutableMap<String, CwtComplexEnumConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.dynamicValueTypes: MutableMap<String, CwtDynamicValueTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.links: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.localisationLinks: MutableMap<@CaseInsensitive String, CwtLinkConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.localisationPromotions: MutableMap<@CaseInsensitive String, CwtLocalisationPromotionConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.scopeGroups: MutableMap<@CaseInsensitive String, CwtScopeGroupConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

val CwtConfigGroup.singleAliases: MutableMap<String, CwtSingleAliasConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.inlineConfigGroup: MutableMap<String, MutableList<CwtInlineConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// name - config
val CwtConfigGroup.modifierCategories: MutableMap<String, CwtModifierCategoryConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - config
val CwtConfigGroup.modifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

// name - config
val CwtConfigGroup.databaseObjectTypes: MutableMap<String, CwtDatabaseObjectTypeConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - configs
val CwtConfigGroup.extendedScriptedVariables: MutableMap<String, CwtExtendedScriptedVariableConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - configs
val CwtConfigGroup.extendedDefinitions: MutableMap<String, MutableList<CwtExtendedDefinitionConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - config
val CwtConfigGroup.extendedGameRules: MutableMap<String, CwtExtendedGameRuleConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - config
val CwtConfigGroup.extendedOnActions: MutableMap<String, CwtExtendedOnActionConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// complex_enum_name - template_expression - config
val CwtConfigGroup.extendedComplexEnumValues: MutableMap<String, MutableMap<String, CwtExtendedComplexEnumValueConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// dynamic_value_type - template_expression - config
val CwtConfigGroup.extendedDynamicValues: MutableMap<String, MutableMap<String, CwtExtendedDynamicValueConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - config
val CwtConfigGroup.extendedInlineScripts: MutableMap<String, CwtExtendedInlineScriptConfig>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// template_expression - configs
val CwtConfigGroup.extendedParameters: MutableMap<String, MutableList<CwtExtendedParameterConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

// endregion

// region Computed

/** 预定义的修正规则的映射。 */
val CwtConfigGroup.predefinedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

/** 生成的修正规则的映射。 */
val CwtConfigGroup.generatedModifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

/** 常量字符串的别名的组名的映射。 */
val CwtConfigGroup.aliasKeysGroupConst: MutableMap<@CaseInsensitive String, MutableMap<String, String>>
    by createKey(CwtConfigGroup.Keys) { caseInsensitiveStringKeyMap() }

/** 非常量字符串的别名的组名的映射。 */
val CwtConfigGroup.aliasKeysGroupNoConst: MutableMap<String, MutableSet<String>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

/** 必定支持作用域的别名规则。 */
val CwtConfigGroup.aliasNamesSupportScope: MutableSet<String>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

/** 相关本地化的模式，用于从本地化导航到相关定义。 */
val CwtConfigGroup.relatedLocalisationPatterns: MutableSet<Tuple2<String, String>>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

/** 获取符合特定条件的排序后的链接规则。 */
val CwtConfigGroup.linksModel: LinksModel
    by createKey(CwtConfigGroup.Keys) { LinksModel() }

/** 获取符合特定条件的排序后的本地化的链接规则。 */
val CwtConfigGroup.localisationLinksModel: LinksModel
    by createKey(CwtConfigGroup.Keys) { LinksModel() }

/** 获取符合特定条件的定义类型。 */
val CwtConfigGroup.definitionTypesModel: DefinitionTypesModel
    by createKey(CwtConfigGroup.Keys) { DefinitionTypesModel() }

// endregion

// region Collected

val CwtConfigGroup.filePathExpressions: MutableSet<CwtDataExpression>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

val CwtConfigGroup.parameterConfigs: MutableSet<CwtMemberConfig<*>>
    by createKey(CwtConfigGroup.Keys) { mutableSetOf() }

// endregion

package icu.windea.pls.config.configGroup

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
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
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy

/**
 * 用于访问规则数据。
 *
 * @see CwtConfigGroup
 * @see CwtConfigGroupInitializer
 */
interface CwtConfigGroupDataHolder {
    fun clear()

    // region Internal

    val schemas: List<CwtSchemaConfig>
    val foldingSettings: Map<String, Map<@CaseInsensitive String, CwtFoldingSettingsConfig>>
    val postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>

    // endregion

    // region Core

    val priorities: Map<String, ParadoxOverrideStrategy>
    val systemScopes: Map<@CaseInsensitive String, CwtSystemScopeConfig>
    val localisationLocalesById: Map<String, CwtLocaleConfig>
    val localisationLocalesByCode: Map<String, CwtLocaleConfig>

    // type - typeConfig
    val types: Map<String, CwtTypeConfig>
    // type - typeConfig
    val swappedTypes: Map<String, CwtTypeConfig>
    // typeExpression - modifierTemplate - modifierConfig
    val type2ModifiersMap: Map<String, Map<String, CwtModifierConfig>>

    // type - declarationConfig
    val declarations: Map<String, CwtDeclarationConfig>

    val rows: Map<String, CwtRowConfig>

    // enumValue 可以是 int、float、bool 类型，统一用字符串表示
    val enums: Map<String, CwtEnumConfig>
    // 基于 enum_name 进行定位，对应的可能是 key/value
    val complexEnums: Map<String, CwtComplexEnumConfig>

    val dynamicValueTypes: Map<String, CwtDynamicValueTypeConfig>

    val links: Map<@CaseInsensitive String, CwtLinkConfig>
    val localisationLinks: Map<@CaseInsensitive String, CwtLinkConfig>
    val localisationCommands: Map<@CaseInsensitive String, CwtLocalisationCommandConfig>
    val localisationPromotions: Map<@CaseInsensitive String, CwtLocalisationPromotionConfig>

    val scopes: Map<@CaseInsensitive String, CwtScopeConfig>
    val scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig>
    val scopeGroups: Map<@CaseInsensitive String, CwtScopeGroupConfig>

    val singleAliases: Map<String, CwtSingleAliasConfig>
    val aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>>
    val directives: List<CwtDirectiveConfig>

    // name - config
    val modifierCategories: Map<String, CwtModifierCategoryConfig>
    // template_expression - config
    val modifiers: Map<@CaseInsensitive String, CwtModifierConfig>

    // name - config
    val databaseObjectTypes: Map<String, CwtDatabaseObjectTypeConfig>

    // endregion

    // region Extended

    // template_expression - configs
    val extendedScriptedVariables: Map<String, CwtExtendedScriptedVariableConfig>
    // template_expression - configs
    val extendedDefinitions: Map<String, List<CwtExtendedDefinitionConfig>>
    // template_expression - config
    val extendedGameRules: Map<String, CwtExtendedGameRuleConfig>
    // template_expression - config
    val extendedOnActions: Map<String, CwtExtendedOnActionConfig>
    // complex_enum_name - template_expression - config
    val extendedComplexEnumValues: Map<String, Map<String, CwtExtendedComplexEnumValueConfig>>
    // dynamic_value_type - template_expression - config
    val extendedDynamicValues: Map<String, Map<String, CwtExtendedDynamicValueConfig>>
    // template_expression - config
    val extendedInlineScripts: Map<String, CwtExtendedInlineScriptConfig>
    // template_expression - configs
    val extendedParameters: Map<String, List<CwtExtendedParameterConfig>>

    // endregion

    // region Computed

    /** 预定义的修正规则的映射。 */
    val predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>
    /** 生成的修正规则的映射。 */
    val generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>

    /** 常量字符串的别名的组名的映射。 */
    val aliasKeysGroupConst: Map<@CaseInsensitive String, Map<@CaseInsensitive String, String>>
    /** 非常量字符串的别名的组名的映射。 */
    val aliasKeysGroupNoConst: Map<String, Set<String>>
    /** 必定支持作用域的别名规则。 */
    val aliasNamesSupportScope: Set<String>

    /** 相关本地化的模式，用于从本地化导航到相关定义。 */
    val relatedLocalisationPatterns: Set<Tuple2<String, String>>

    /** 获取符合特定条件的链接规则。 */
    val linksModel: CwtLinksModel
    /** 获取符合特定条件的本地化的链接规则。 */
    val localisationLinksModel: CwtLinksModel
    /** 获取符合特定条件的指令规则。 */
    val directivesModel: CwtDirectivesModel
    /** 获取符合特定条件的定义类型。 */
    val definitionTypesModel: CwtDefinitionTypesModel

    // endregion

    // region Collected

    val filePathExpressions: Set<CwtDataExpression>
    val parameterConfigs: MutableSet<CwtMemberConfig<*>>

    // endregion
}

/** 用于获取符合特定条件的链接规则。 */
interface CwtLinksModel {
    /** 变量对应的链接规则的列表。 */
    val variable: List<CwtLinkConfig>
    val forScopeStatic: List<CwtLinkConfig>
    val forScopeNoPrefixSorted: List<CwtLinkConfig>
    val forScopeFromDataSorted: List<CwtLinkConfig>
    val forScopeFromArgumentSorted: List<CwtLinkConfig>
    val forScopeFromArgumentSortedByPrefix: Map<String, List<CwtLinkConfig>>
    val forValueStatic: List<CwtLinkConfig>
    val forValueNoPrefixSorted: List<CwtLinkConfig>
    val forValueFromDataSorted: List<CwtLinkConfig>
    val forValueFromArgumentSorted: List<CwtLinkConfig>
    val forValueFromArgumentSortedByPrefix: Map<String, List<CwtLinkConfig>>
}

/** 用于获取符合特定条件的指令规则。 */
interface CwtDirectivesModel {
    val inlineScript: List<CwtDirectiveConfig>
    val definitionInjection: CwtDirectiveConfig?
}

/** 用于获取符合特定条件的定义类型。 */
interface CwtDefinitionTypesModel {
    /** 必定支持作用域的定义类型。 */
    val supportScope: Set<String>
    /** 必定间接支持作用域的定义类型。 */
    val indirectSupportScope: Set<String>
    /** 不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）。 */
    val skipCheckSystemScope: Set<String>
    /** 支持参数的定义类型。 */
    val supportParameters: Set<String>
    /** 可能有类型键前缀（type_key_prefix）的定义类型 - 按文件路径计算。 */
    val typeKeyPrefixAware: Set<String>
}

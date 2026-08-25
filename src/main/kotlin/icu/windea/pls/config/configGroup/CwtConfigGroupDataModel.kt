package icu.windea.pls.config.configGroup

import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.config.attributes.CwtConfigGroupAttributes
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributes
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.extended.CwtExtendedParameterConfig
import icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.config.config.CwtConfigPostProcessor
import icu.windea.pls.ep.resolve.localisation.ParadoxCompositeLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

/**
 * 规则分组的数据模型。
 *
 * 除了直接来自规则文件的那些数据外，也包括计算得到的数据，收集得到的数据，以及规则分组自身的综合属性。
 *
 * 参考：
 * - 规则系统的说明文档：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 * - 规则格式的参考手册：[ref-config-format.md](https://windea.icu/Paradox-Language-Support/ref-config-format.md)
 *
 * @see CwtConfigGroup
 */
interface CwtConfigGroupDataModel {
    /**
     * 得到原始的文件规则映射，键为相对于规则分组根目录的路径。
     *
     * 备注：默认不保留。参见 [ChronicleCapacities.keepFileConfigs]。
     */
    val fileConfigs: Map<String, CwtFileConfig> get() = emptyMap()

    /**
     * @see CwtConfigPostProcessor
     */
    val configPostProcessActions: List<Runnable> get() = emptyList()

    val schemas: List<CwtSchemaConfig> get() = emptyList()
    val foldingSettings: Map<String, Map<@CaseInsensitive String, CwtFoldingSettingsConfig>> get() = emptyMap()
    val postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>> get() = emptyMap()

    // region Core

    val priorities: Map<String, ParadoxOverrideStrategy> get() = emptyMap()
    val systemScopes: Map<@CaseInsensitive String, CwtSystemScopeConfig> get() = emptyMap()
    val locales: Map<String, CwtLocaleConfig> get() = emptyMap()

    // type - typeConfig
    val types: Map<String, CwtTypeConfig> get() = emptyMap()
    // type - typeConfig
    val swappedTypes: Map<String, CwtTypeConfig> get() = emptyMap()
    // typeExpression - modifierTemplate - modifierConfig
    val type2ModifiersMap: Map<String, Map<String, CwtModifierConfig>> get() = emptyMap()

    // type - declarationConfig
    val declarations: Map<String, CwtDeclarationConfig> get() = emptyMap()

    val rows: Map<String, CwtRowConfig> get() = emptyMap()

    val defineNamespaces: Map<String, CwtDefineNamespaceConfig> get() = emptyMap()

    // enumValue 可以是 int、float、bool 类型，统一用字符串表示
    val enums: Map<String, CwtEnumConfig> get() = emptyMap()
    // 基于 enum_name 进行定位，对应的可能是 key/value
    val complexEnums: Map<String, CwtComplexEnumConfig> get() = emptyMap()
    // 来自列规则的复杂枚举规则，在 CSV 文件中声明（也包含在 complexEnums 中）
    val complexEnumsFromColumns: Map<String, CwtComplexEnumConfig> get() = emptyMap()

    val unions: Map<String, CwtUnionConfig> get() = emptyMap()

    val dynamicValueTypes: Map<String, CwtDynamicValueTypeConfig> get() = emptyMap()

    val links: Map<@CaseInsensitive String, CwtLinkConfig> get() = emptyMap()
    val localisationLinks: Map<@CaseInsensitive String, CwtLinkConfig> get() = emptyMap()
    val localisationCommands: Map<@CaseInsensitive String, CwtLocalisationCommandConfig> get() = emptyMap()
    val localisationPromotions: Map<@CaseInsensitive String, CwtLocalisationPromotionConfig> get() = emptyMap()

    val scopes: Map<@CaseInsensitive String, CwtScopeConfig> get() = emptyMap()
    val scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig> get() = emptyMap()
    val scopeGroups: Map<@CaseInsensitive String, CwtScopeGroupConfig> get() = emptyMap()

    // template_expression - config
    val modifiers: Map<@CaseInsensitive String, CwtModifierConfig> get() = emptyMap()
    // name - config
    val modifierCategories: Map<@CaseInsensitive String, CwtModifierCategoryConfig> get() = emptyMap()

    // name - config
    val databaseObjectTypes: Map<String, CwtDatabaseObjectTypeConfig> get() = emptyMap()

    val aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>> get() = emptyMap()
    val singleAliases: Map<String, CwtSingleAliasConfig> get() = emptyMap()

    val macros: List<CwtMacroConfig> get() = emptyList()

    // endregion

    // region Extended

    // pattern - configs
    val extendedScriptedVariables: Map<String, CwtExtendedScriptedVariableConfig> get() = emptyMap()
    // pattern - configs
    val extendedDefinitions: Map<String, List<CwtExtendedDefinitionConfig>> get() = emptyMap()
    // pattern - config
    val extendedGameRules: Map<String, CwtExtendedGameRuleConfig> get() = emptyMap()
    // pattern - config
    val extendedOnActions: Map<String, CwtExtendedOnActionConfig> get() = emptyMap()
    // pattern - configs
    val extendedParameters: Map<String, List<CwtExtendedParameterConfig>> get() = emptyMap()
    // enum_name - pattern - config
    val extendedComplexEnumValues: Map<String, Map<String, CwtExtendedComplexEnumValueConfig>> get() = emptyMap()
    // dynamic_value_type - pattern - config
    val extendedDynamicValues: Map<String, Map<String, CwtExtendedDynamicValueConfig>> get() = emptyMap()
    // pattern - config
    val extendedInlineScripts: Map<String, CwtExtendedInlineScriptConfig> get() = emptyMap()

    // endregion

    // region Computed

    /** 全局的语言环境规则的列表。其中部分可能不受当前游戏类型支持。 */
    val globalLocales: List<CwtLocaleConfig> get() = emptyList()
    /** 支持的语言环境规则的列表。 */
    val supportedLocales: List<CwtLocaleConfig> get() = emptyList()

    /** 预定义的修正规则的映射。 */
    val predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig> get() = emptyMap() // 3.0.1 clarify: ignore case (for modifier names)
    /** 生成的修正规则的映射。 */
    val generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig> get() = emptyMap() // 3.0.1 clarify: ignore case (for modifier names)

    /** 常量字符串的别名的组名的映射。 */
    val aliasKeysGroupConst: Map<@CaseInsensitive String, Map<@CaseInsensitive String, String>> get() = emptyMap()
    /** 非常量字符串的别名的组名的映射。 */
    val aliasKeysGroupNoConst: Map<String, Set<String>> get() = emptyMap()
    /** 必定支持作用域的别名规则。 */
    val aliasNamesSupportScope: Set<String> get() = emptySet()

    /** 相关本地化的模式，用于从本地化导航到相关定义。 */
    val relatedLocalisationPatterns: Set<Tuple2<String, String>> get() = emptySet()

    // endregion

    // region Models

    /** 获取符合特定条件的定义类型。 */
    val typeModel: CwtTypeModel get() = CwtTypeModel.Empty
    /** 获取作用域的关系信息。 */
    val scopeModel: CwtScopeModel get() = CwtScopeModel.Empty
    /** 获取符合特定条件的链接规则。 */
    val linkModel: CwtLinkModel get() = CwtLinkModel.Empty
    /** 获取符合特定条件的本地化的链接规则。 */
    val localisationLinkModel: CwtLinkModel get() = CwtLinkModel.Empty
    /** 获取符合特定条件的宏规则。 */
    val macroModel: CwtMacroModel get() = CwtMacroModel.Empty

    // endregion

    // region Attributes

    /** 规则分组自身的综合属性。 */
    val attribute: CwtConfigGroupAttributes get() = CwtConfigGroupAttributes.EMPTY
    /** 得到指定名字的并集规则（[CwtUnionConfig]）的综合属性。 */
    fun getUnionAttribute(name: String): CwtExpandableConfigAttributes = CwtExpandableConfigAttributes.EMPTY
    /** 得到指定名字的别名规则（[CwtAliasConfig]）的综合属性。 */
    fun getAliasAttribute(name: String): CwtExpandableConfigAttributes = CwtExpandableConfigAttributes.EMPTY
    /** 得到指定名字的单别名规则（[CwtSingleAliasConfig]）的综合属性。 */
    fun getSingleAliasAttribute(name: String): CwtExpandableConfigAttributes = CwtExpandableConfigAttributes.EMPTY

    // endregion

    object Empty : CwtConfigGroupDataModel
}

/** 定义类型的数据模型。用于保存和获取符合特定条件的定义类型。 */
interface CwtTypeModel {
    /** 基础类型到切换类型的映射。 */
    val base2Swapped: Map<String, String> get() = emptyMap()
    /** 切换类型到基础类型的映射。 */
    val swapped2Base: Map<String, String> get() = emptyMap()
    /** 支持作用域的定义类型。 */
    val supportScope: Set<String> get() = emptySet()
    /** 间接支持作用域的定义类型。 */
    val supportIndirectScope: Set<String> get() = emptySet()
    /** 支持作用域推断的定义类型。 */
    val supportScopeInference: Set<String> get() = emptySet()
    /** 不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）。 */
    val skipCheckSystemScope: Set<String> get() = emptySet()
    /** 支持参数的定义类型。 */
    val supportParameters: Set<String> get() = emptySet()
    /**
     * 可能有类型键前缀（typeKeyPrefix）的定义类型 - 按文件路径计算。
     *
     * @see CwtTypeConfig.typeKeyPrefix
     */
    val typeKeyPrefixAware: Set<String> get() = emptySet()
    /**
     * 可能作为本地化图标的解析目标的定义类型。
     *
     * @see ParadoxLocalisationIconPsiReference
     * @see ParadoxLocalisationIconSupport
     * @see ParadoxCompositeLocalisationIconSupport.fromDefinition
     * @see ParadoxDefinitionIndexConstraint.LocalisationIconResolvable
     */
    val localisationIconResolvable: Set<String> get() = emptySet()

    object Empty : CwtTypeModel
}

/** 作用域的数据模型。用于保存和获取作用域的关系信息（别名、继承等），以优化匹配和合并作用域时的性能。 */
interface CwtScopeModel {
    /** 基础作用域到匹配其别名的作用域的集合（仅保存索引计数）。 */
    val base2Aliases: Map<Int, Set<Int>> get() = emptyMap()
    /** 基础作用域到父作用域的集合（仅保存索引计数，兼容别名形式）。 */
    val base2Parents: Map<Int, Set<Int>> get() = emptyMap()
    // /** 基础作用域到提升后的作用域的集合（仅保存索引计数，兼容别名形式）。 */
    // val base2Promotions: Map<Int, Set<Int>> get() = emptyMap()
    /** 基础作用域到匹配的作用域的集合（仅保存索引计数，兼容别名形式）。 */
    val base2MatchedScopes: Map<Int, Set<Int>> get() = emptyMap()

    object Empty : CwtScopeModel
}

/** 链接规则的数据模型。用于保存和获取符合特定条件的链接规则。 */
interface CwtLinkModel {
    /** 变量对应的链接规则的列表。 */
    val variable: List<CwtLinkConfig> get() = emptyList()
    val forScopeStatic: List<CwtLinkConfig> get() = emptyList()
    val forScopeNoPrefixSorted: List<CwtLinkConfig> get() = emptyList()
    val forScopeFromDataSorted: List<CwtLinkConfig> get() = emptyList()
    val forScopeFromArgumentSorted: List<CwtLinkConfig> get() = emptyList()
    val forScopeFromArgumentSortedByPrefix: Map<@CaseInsensitive String, List<CwtLinkConfig>> get() = emptyMap()
    val forValueStatic: List<CwtLinkConfig> get() = emptyList()
    val forValueNoPrefixSorted: List<CwtLinkConfig> get() = emptyList()
    val forValueFromDataSorted: List<CwtLinkConfig> get() = emptyList()
    val forValueFromArgumentSorted: List<CwtLinkConfig> get() = emptyList()
    val forValueFromArgumentSortedByPrefix: Map<@CaseInsensitive String, List<CwtLinkConfig>> get() = emptyMap()

    object Empty : CwtLinkModel
}

/** 宏规则的数据模型。用于保存和获取符合特定条件的宏规则。 */
interface CwtMacroModel {
    val forInlineScripts: List<CwtMacroConfig.InlineScript> get() = emptyList()
    val forDefinitionInjections: CwtMacroConfig.DefinitionInjection? get() = null

    object Empty : CwtMacroModel
}

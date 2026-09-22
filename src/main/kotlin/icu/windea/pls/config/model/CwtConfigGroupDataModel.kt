package icu.windea.pls.config.model

import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.config.attributes.CwtConfigGroupAttributes
import icu.windea.pls.config.attributes.CwtConfigGroupAttributesBase
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributes
import icu.windea.pls.config.attributes.CwtExpandableConfigAttributesEvaluator
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
import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

/**
 * 规则分组的数据模型。
 *
 * 除了直接来自规则文件的那些数据外，也包括计算得到的数据，收集得到的数据，以及规则分组自身的综合属性。
 *
 * 参考：
 * - 规则系统的说明文档：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 * - 规则格式的参考手册：[ref-config-format.md](https://windea.icu/Paradox-Language-Support/ref-config-format.md)
 *
 * @see icu.windea.pls.config.configGroup.CwtConfigGroup
 */
interface CwtConfigGroupDataModel {
    /**
     * 得到原始的文件规则映射，键为相对于规则分组根目录的路径。
     *
     * 备注：默认不保留。参见 [ChronicleCapacities.keepFileConfigs]。
     */
    val fileConfigs: Map<String, CwtFileConfig>

    /**
     * @see icu.windea.pls.ep.config.config.CwtConfigPostProcessor
     */
    val configPostProcessActions: List<Runnable>

    val schemas: List<CwtSchemaConfig>
    val foldingSettings: Map<String, Map<@CaseInsensitive String, CwtFoldingSettingsConfig>>
    val postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>

    // region Core

    val priorities: Map<String, ParadoxOverrideStrategy>
    val systemScopes: Map<@CaseInsensitive String, CwtSystemScopeConfig>
    val locales: Map<String, CwtLocaleConfig>

    // type - typeConfig
    val types: Map<String, CwtTypeConfig>
    // type - typeConfig
    val swappedTypes: Map<String, CwtTypeConfig>
    // typeExpression - modifierTemplate - modifierConfig
    val type2ModifiersMap: Map<String, Map<String, CwtModifierConfig>>

    // type - declarationConfig
    val declarations: Map<String, CwtDeclarationConfig>

    val rows: Map<String, CwtRowConfig>

    val defineNamespaces: Map<String, CwtDefineNamespaceConfig>

    // enumValue 可以是 int、float、bool 类型，统一用字符串表示
    val enums: Map<String, CwtEnumConfig>
    // 基于 enum_name 进行定位，对应的可能是 key/value
    val complexEnums: Map<String, CwtComplexEnumConfig>
    // 来自列规则的复杂枚举规则，在 CSV 文件中声明（也包含在 complexEnums 中）
    val complexEnumsFromColumns: Map<String, CwtComplexEnumConfig>

    val unions: Map<String, CwtUnionConfig>

    val dynamicValueTypes: Map<String, CwtDynamicValueTypeConfig>

    val links: Map<@CaseInsensitive String, CwtLinkConfig>
    val localisationLinks: Map<@CaseInsensitive String, CwtLinkConfig>
    val localisationCommands: Map<@CaseInsensitive String, CwtLocalisationCommandConfig>
    val localisationPromotions: Map<@CaseInsensitive String, CwtLocalisationPromotionConfig>

    val scopes: Map<@CaseInsensitive String, CwtScopeConfig>
    val scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig>
    val scopeGroups: Map<@CaseInsensitive String, CwtScopeGroupConfig>

    // template_expression - config
    val modifiers: Map<@CaseInsensitive String, CwtModifierConfig>
    // name - config
    val modifierCategories: Map<@CaseInsensitive String, CwtModifierCategoryConfig>

    // name - config
    val databaseObjectTypes: Map<String, CwtDatabaseObjectTypeConfig>

    val aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>>
    val singleAliases: Map<String, CwtSingleAliasConfig>

    val macros: List<CwtMacroConfig>

    // endregion

    // region Extended

    // pattern - configs
    val extendedScriptedVariables: Map<String, CwtExtendedScriptedVariableConfig>
    // pattern - configs
    val extendedDefinitions: Map<String, List<CwtExtendedDefinitionConfig>>
    // pattern - config
    val extendedGameRules: Map<String, CwtExtendedGameRuleConfig>
    // pattern - config
    val extendedOnActions: Map<String, CwtExtendedOnActionConfig>
    // pattern - configs
    val extendedParameters: Map<String, List<CwtExtendedParameterConfig>>
    // enum_name - pattern - config
    val extendedComplexEnumValues: Map<String, Map<String, CwtExtendedComplexEnumValueConfig>>
    // dynamic_value_type - pattern - config
    val extendedDynamicValues: Map<String, Map<String, CwtExtendedDynamicValueConfig>>
    // pattern - config
    val extendedInlineScripts: Map<String, CwtExtendedInlineScriptConfig>

    // endregion

    // region Computed

    /** 全局的语言环境规则的列表。其中部分可能不受当前游戏类型支持。 */
    val globalLocales: List<CwtLocaleConfig>
    /** 支持的语言环境规则的列表。 */
    val supportedLocales: List<CwtLocaleConfig>

    /** 预定义的修正规则的映射。 */
    val predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>
    /** 生成的修正规则的映射。 */
    val generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>

    /** 相关本地化的模式，用于从本地化导航到相关定义。 */
    val relatedLocalisationPatterns: Set<Tuple2<String, String>>

    // endregion

    // region Models

    /** 获取符合特定条件的定义类型。 */
    val typeModel: CwtTypeModel
    /** 获取作用域的关系信息。 */
    val scopeModel: CwtScopeModel
    /** 获取符合特定条件的链接规则。 */
    val linkModel: CwtLinkModel
    /** 获取符合特定条件的本地化的链接规则。 */
    val localisationLinkModel: CwtLinkModel
    /** 获取符合特定条件的别名规则的名字和键。 */
    val aliasModel: CwtAliasModel
    /** 获取符合特定条件的并集规则的名字和键。 */
    val unionModel: CwtUnionModel
    /** 获取符合特定条件的宏规则。 */
    val macroModel: CwtMacroModel

    // endregion

    // region Attributes

    /** 规则分组自身的综合属性。 */
    val attributes: CwtConfigGroupAttributes
    /** 得到指定名字的并集规则（[CwtUnionConfig]）的综合属性。 */
    fun getUnionAttribute(name: String): CwtExpandableConfigAttributes
    /** 得到指定名字的别名规则（[CwtAliasConfig]）的综合属性。 */
    fun getAliasAttribute(name: String): CwtExpandableConfigAttributes
    /** 得到指定名字的单别名规则（[CwtSingleAliasConfig]）的综合属性。 */
    fun getSingleAliasAttribute(name: String): CwtExpandableConfigAttributes

    // endregion

    companion object {
        @JvmStatic
        fun create(): CwtConfigGroupDataModelBase = CwtConfigGroupDataModelBase()

        @JvmStatic
        fun createEmpty(): CwtConfigGroupDataModel = EmptyCwtConfigGroupDataModel
    }
}

// region Implementations

class CwtConfigGroupDataModelBase : CwtConfigGroupDataModel {
    override val fileConfigs = Object2ObjectLinkedOpenHashMap<String, CwtFileConfig>()
    override val configPostProcessActions = ObjectArrayList<Runnable>()
    override val schemas = ObjectArrayList<CwtSchemaConfig>()
    override val foldingSettings = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>()
    override val postfixTemplateSettings = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>()
    override val priorities = Object2ObjectLinkedOpenHashMap<String, ParadoxOverrideStrategy>()
    override val systemScopes = CaseInsensitiveStringKeyMap<CwtSystemScopeConfig>()
    override val locales = Object2ObjectLinkedOpenHashMap<String, CwtLocaleConfig>()
    override val types = Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig>()
    override val swappedTypes = Object2ObjectLinkedOpenHashMap<String, CwtTypeConfig>()
    override val type2ModifiersMap = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtModifierConfig>>()
    override val declarations = Object2ObjectLinkedOpenHashMap<String, CwtDeclarationConfig>()
    override val rows = Object2ObjectLinkedOpenHashMap<String, CwtRowConfig>()
    override val defineNamespaces = Object2ObjectLinkedOpenHashMap<String, CwtDefineNamespaceConfig>()
    override val enums = Object2ObjectLinkedOpenHashMap<String, CwtEnumConfig>()
    override val complexEnums = Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig>()
    override val complexEnumsFromColumns = Object2ObjectLinkedOpenHashMap<String, CwtComplexEnumConfig>()
    override val unions = Object2ObjectLinkedOpenHashMap<String, CwtUnionConfig>()
    override val dynamicValueTypes = Object2ObjectLinkedOpenHashMap<String, CwtDynamicValueTypeConfig>()
    override val links = CaseInsensitiveStringKeyMap<CwtLinkConfig>()
    override val localisationLinks = CaseInsensitiveStringKeyMap<CwtLinkConfig>()
    override val localisationCommands = CaseInsensitiveStringKeyMap<CwtLocalisationCommandConfig>()
    override val localisationPromotions = CaseInsensitiveStringKeyMap<CwtLocalisationPromotionConfig>()
    override val scopes = CaseInsensitiveStringKeyMap<CwtScopeConfig>()
    override val scopeAliasMap = CaseInsensitiveStringKeyMap<CwtScopeConfig>()
    override val scopeGroups = CaseInsensitiveStringKeyMap<CwtScopeGroupConfig>()
    override val aliasGroups = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtAliasConfig>>>()
    override val singleAliases = Object2ObjectLinkedOpenHashMap<String, CwtSingleAliasConfig>()
    override val modifiers = CaseInsensitiveStringKeyMap<CwtModifierConfig>()
    override val modifierCategories = CaseInsensitiveStringKeyMap<CwtModifierCategoryConfig>()
    override val databaseObjectTypes = Object2ObjectLinkedOpenHashMap<String, CwtDatabaseObjectTypeConfig>()
    override val macros = ObjectArrayList<CwtMacroConfig>()
    override val extendedScriptedVariables = Object2ObjectLinkedOpenHashMap<String, CwtExtendedScriptedVariableConfig>()
    override val extendedDefinitions = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedDefinitionConfig>>()
    override val extendedGameRules = Object2ObjectLinkedOpenHashMap<String, CwtExtendedGameRuleConfig>()
    override val extendedOnActions = Object2ObjectLinkedOpenHashMap<String, CwtExtendedOnActionConfig>()
    override val extendedParameters = Object2ObjectLinkedOpenHashMap<String, ObjectArrayList<CwtExtendedParameterConfig>>()
    override val extendedComplexEnumValues = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedComplexEnumValueConfig>>()
    override val extendedDynamicValues = Object2ObjectLinkedOpenHashMap<String, Object2ObjectLinkedOpenHashMap<String, CwtExtendedDynamicValueConfig>>()
    override val extendedInlineScripts = Object2ObjectLinkedOpenHashMap<String, CwtExtendedInlineScriptConfig>()
    override val globalLocales = ObjectArrayList<CwtLocaleConfig>()
    override val supportedLocales = ObjectArrayList<CwtLocaleConfig>()
    override val predefinedModifiers = CaseInsensitiveStringKeyMap<CwtModifierConfig>()
    override val generatedModifiers = CaseInsensitiveStringKeyMap<CwtModifierConfig>()
    override val relatedLocalisationPatterns = ObjectLinkedOpenHashSet<Tuple2<String, String>>()
    override val typeModel = CwtTypeModel.create()
    override val scopeModel = CwtScopeModel.create()
    override val linkModel = CwtLinkModel.create()
    override val localisationLinkModel = CwtLinkModel.create()
    override val aliasModel = CwtAliasModel.create()
    override val unionModel = CwtUnionModel.create()
    override val macroModel = CwtMacroModel.create()
    override val attributes = CwtConfigGroupAttributesBase()
    val unionAttributes = Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>()
    val aliasAttributes = Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>()
    val singleAliasAttributes = Object2ObjectLinkedOpenHashMap<String, CwtExpandableConfigAttributes>()

    override fun getUnionAttribute(name: String): CwtExpandableConfigAttributes {
        return unionAttributes.getOrPut(name) {
            val config = unions[name] ?: return CwtExpandableConfigAttributes.EMPTY
            val configGroup = config.configGroup
            CwtExpandableConfigAttributesEvaluator().evaluate(name, config, configGroup)
        }
    }

    override fun getAliasAttribute(name: String): CwtExpandableConfigAttributes {
        return aliasAttributes.getOrPut(name) {
            val configs = aliasGroups[name]?.values ?: return CwtExpandableConfigAttributes.EMPTY
            if (configs.isEmpty()) return CwtExpandableConfigAttributes.EMPTY
            val configGroup = configs.firstOrNull()?.firstOrNull()?.configGroup ?: return CwtExpandableConfigAttributes.EMPTY
            CwtExpandableConfigAttributesEvaluator().evaluate(name, configs, configGroup)
        }
    }

    override fun getSingleAliasAttribute(name: String): CwtExpandableConfigAttributes {
        return singleAliasAttributes.getOrPut(name) {
            val config = singleAliases[name] ?: return CwtExpandableConfigAttributes.EMPTY
            val configGroup = config.configGroup
            CwtExpandableConfigAttributesEvaluator().evaluate(name, config, configGroup)
        }
    }

    fun trim() {
        schemas.trim()
        foldingSettings.trim()
        foldingSettings.values.forEach { it.trim() }
        postfixTemplateSettings.trim()
        postfixTemplateSettings.values.forEach { it.trim() }
        priorities.trim()
        systemScopes.trim()
        locales.trim()
        types.trim()
        swappedTypes.trim()
        type2ModifiersMap.trim()
        type2ModifiersMap.values.forEach { it.trim() }
        declarations.trim()
        rows.trim()
        defineNamespaces.trim()
        enums.trim()
        complexEnums.trim()
        complexEnumsFromColumns.trim()
        unions.trim()
        dynamicValueTypes.trim()
        links.trim()
        localisationLinks.trim()
        localisationCommands.trim()
        localisationPromotions.trim()
        scopes.trim()
        scopeAliasMap.trim()
        scopeGroups.trim()
        aliasGroups.trim()
        aliasGroups.values.forEach { it.trim() }
        singleAliases.trim()
        macros.trim()
        modifiers.trim()
        modifierCategories.trim()
        databaseObjectTypes.trim()
        extendedScriptedVariables.trim()
        extendedDefinitions.trim()
        extendedDefinitions.values.forEach { it.trim() }
        extendedGameRules.trim()
        extendedOnActions.trim()
        extendedParameters.trim()
        extendedParameters.values.forEach { it.trim() }
        extendedComplexEnumValues.trim()
        extendedComplexEnumValues.values.forEach { it.trim() }
        extendedDynamicValues.trim()
        extendedDynamicValues.values.forEach { it.trim() }
        extendedInlineScripts.trim()
        globalLocales.trim()
        supportedLocales.trim()
        predefinedModifiers.trim()
        generatedModifiers.trim()
        relatedLocalisationPatterns.trim()

        typeModel.trim()
        scopeModel.trim()
        linkModel.trim()
        localisationLinkModel.trim()
        aliasModel.trim()
        unionModel.trim()
        macroModel.trim()

        attributes.trim()
        unionAttributes.trim()
        aliasAttributes.trim()
        singleAliasAttributes.trim()
    }
}

private object EmptyCwtConfigGroupDataModel : CwtConfigGroupDataModel {
    override val fileConfigs: Map<String, CwtFileConfig> get() = emptyMap()
    override val configPostProcessActions: List<Runnable> get() = emptyList()
    override val schemas: List<CwtSchemaConfig> get() = emptyList()
    override val foldingSettings: Map<String, Map<@CaseInsensitive String, CwtFoldingSettingsConfig>> get() = emptyMap()
    override val postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>> get() = emptyMap()
    override val priorities: Map<String, ParadoxOverrideStrategy> get() = emptyMap()
    override val systemScopes: Map<@CaseInsensitive String, CwtSystemScopeConfig> get() = emptyMap()
    override val locales: Map<String, CwtLocaleConfig> get() = emptyMap()
    override val types: Map<String, CwtTypeConfig> get() = emptyMap()
    override val swappedTypes: Map<String, CwtTypeConfig> get() = emptyMap()
    override val type2ModifiersMap: Map<String, Map<String, CwtModifierConfig>> get() = emptyMap()
    override val declarations: Map<String, CwtDeclarationConfig> get() = emptyMap()
    override val rows: Map<String, CwtRowConfig> get() = emptyMap()
    override val defineNamespaces: Map<String, CwtDefineNamespaceConfig> get() = emptyMap()
    override val enums: Map<String, CwtEnumConfig> get() = emptyMap()
    override val complexEnums: Map<String, CwtComplexEnumConfig> get() = emptyMap()
    override val complexEnumsFromColumns: Map<String, CwtComplexEnumConfig> get() = emptyMap()
    override val unions: Map<String, CwtUnionConfig> get() = emptyMap()
    override val dynamicValueTypes: Map<String, CwtDynamicValueTypeConfig> get() = emptyMap()
    override val links: Map<@CaseInsensitive String, CwtLinkConfig> get() = emptyMap()
    override val localisationLinks: Map<@CaseInsensitive String, CwtLinkConfig> get() = emptyMap()
    override val localisationCommands: Map<@CaseInsensitive String, CwtLocalisationCommandConfig> get() = emptyMap()
    override val localisationPromotions: Map<@CaseInsensitive String, CwtLocalisationPromotionConfig> get() = emptyMap()
    override val scopes: Map<@CaseInsensitive String, CwtScopeConfig> get() = emptyMap()
    override val scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig> get() = emptyMap()
    override val scopeGroups: Map<@CaseInsensitive String, CwtScopeGroupConfig> get() = emptyMap()
    override val aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>> get() = emptyMap()
    override val singleAliases: Map<String, CwtSingleAliasConfig> get() = emptyMap()
    override val modifiers: Map<@CaseInsensitive String, CwtModifierConfig> get() = emptyMap()
    override val modifierCategories: Map<@CaseInsensitive String, CwtModifierCategoryConfig> get() = emptyMap()
    override val databaseObjectTypes: Map<String, CwtDatabaseObjectTypeConfig> get() = emptyMap()
    override val macros: List<CwtMacroConfig> get() = emptyList()
    override val extendedScriptedVariables: Map<String, CwtExtendedScriptedVariableConfig> get() = emptyMap()
    override val extendedDefinitions: Map<String, List<CwtExtendedDefinitionConfig>> get() = emptyMap()
    override val extendedGameRules: Map<String, CwtExtendedGameRuleConfig> get() = emptyMap()
    override val extendedOnActions: Map<String, CwtExtendedOnActionConfig> get() = emptyMap()
    override val extendedParameters: Map<String, List<CwtExtendedParameterConfig>> get() = emptyMap()
    override val extendedComplexEnumValues: Map<String, Map<String, CwtExtendedComplexEnumValueConfig>> get() = emptyMap()
    override val extendedDynamicValues: Map<String, Map<String, CwtExtendedDynamicValueConfig>> get() = emptyMap()
    override val extendedInlineScripts: Map<String, CwtExtendedInlineScriptConfig> get() = emptyMap()
    override val globalLocales: List<CwtLocaleConfig> get() = emptyList()
    override val supportedLocales: List<CwtLocaleConfig> get() = emptyList()
    override val predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig> get() = emptyMap()
    override val generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig> get() = emptyMap()
    override val relatedLocalisationPatterns: Set<Tuple2<String, String>> get() = emptySet()
    override val typeModel: CwtTypeModel get() = CwtTypeModel.createEmpty()
    override val scopeModel: CwtScopeModel get() = CwtScopeModel.createEmpty()
    override val linkModel: CwtLinkModel get() = CwtLinkModel.createEmpty()
    override val localisationLinkModel: CwtLinkModel get() = CwtLinkModel.createEmpty()
    override val aliasModel: CwtAliasModel get() = CwtAliasModel.createEmpty()
    override val unionModel: CwtUnionModel get() = CwtUnionModel.createEmpty()
    override val macroModel: CwtMacroModel get() = CwtMacroModel.createEmpty()

    override val attributes: CwtConfigGroupAttributes get() = CwtConfigGroupAttributes.EMPTY
    override fun getUnionAttribute(name: String): CwtExpandableConfigAttributes = CwtExpandableConfigAttributes.EMPTY
    override fun getAliasAttribute(name: String): CwtExpandableConfigAttributes = CwtExpandableConfigAttributes.EMPTY
    override fun getSingleAliasAttribute(name: String): CwtExpandableConfigAttributes = CwtExpandableConfigAttributes.EMPTY
}

// endregion

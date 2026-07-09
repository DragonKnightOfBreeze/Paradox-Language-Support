package icu.windea.pls.config

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtConfigTypes.Effect
import icu.windea.pls.config.CwtConfigTypes.Modifier
import icu.windea.pls.config.CwtConfigTypes.Trigger
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig
import icu.windea.pls.config.config.delegated.CwtDefineVariableConfig
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
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
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

/**
 * 所有预定义的规则类型。
 *
 * 每个规则类型对应规则文件中一种特定的声明结构，由其在规则文件中的路径模式决定。
 *
 * @see CwtConfigType
 */
@Suppress("unused")
object CwtConfigTypes {
    // NOTE 2.1.8 偏好使用 lambda 式构建器，而非多行的链式构建器：可通过代码折叠隐藏细节，方便查看

    // region Standard Config Types

    /**
     * 类型规则。
     *
     * 路径定位：
     * - `types/type[{type}]`。其中 `{type}` 匹配类型名（即规则名称）。
     *
     * @see CwtTypeConfig
     */
    val Type = CwtConfigType.builder("Type").build {
        icon(ChronicleIcons.Configs.Type)
        prefix("(type)")
        description(ChronicleBundle.message("config.description.type"))
    }
    /**
     * 子类型规则。
     *
     * 路径定位：
     * - `types/type[{type}]/subtype[{subtype}]`。其中 `{type}` 匹配类型名，`{subtype}` 匹配子类型名（即规则名称）。
     *
     * @see CwtSubtypeConfig
     */
    val Subtype = CwtConfigType.builder("Subtype").build {
        icon(ChronicleIcons.Configs.Type)
        prefix("(subtype)")
        description(ChronicleBundle.message("config.description.subtype"))
    }
    /**
     * 行规则。
     *
     * 路径定位：
     * - `rows/row[{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtRowConfig
     */
    val Row = CwtConfigType.builder("Row").build {
        icon(ChronicleIcons.Configs.Row)
        prefix("(row)")
        description(ChronicleBundle.message("config.description.row"))
    }
    /**
     * 定值命名空间规则。
     *
     * 路径定位：
     * - `defines/{namespace}`。其中 `{namespace}` 匹配命名空间（即规则名称）。
     *
     * @see CwtDefineNamespaceConfig
     */
    val DefineNamespace = CwtConfigType.builder("DefineNamespace").build {
        icon(ChronicleIcons.Configs.DefineNamespace)
        prefix("(define namespace)")
        description(ChronicleBundle.message("config.description.defineNamespace"))
    }
    /**
     * 定值变量规则。
     *
     * 路径定位：
     * - `defines/{namespace}/{variable}`。其中 `{namespace}` 匹配命名空间，`variable` 匹配变量名（即规则名称）。
     *
     * @see CwtDefineVariableConfig
     */
    val DefineVariable = CwtConfigType.builder("DefineVariable").build {
        icon(ChronicleIcons.Configs.DefineVariable)
        prefix("(define variable)")
        description(ChronicleBundle.message("config.description.defineVariable"))
    }
    /**
     * 枚举规则。
     *
     * 路径定位：
     * - `enums/enum[{name}]`。其中 `{name}` 匹配规则名称（即枚举名）。
     *
     * @see CwtEnumConfig
     */
    val Enum = CwtConfigType.builder("Enum").build {
        icon(ChronicleIcons.Configs.Enum)
        prefix("(enum)")
        description(ChronicleBundle.message("config.description.enum"))
    }
    /**
     * 枚举值规则。
     *
     * 路径定位：
     * - `enums/enum[{enum}]/-`。其中 {enum} 匹配枚举名。
     *
     * @see CwtEnumConfig
     */
    val EnumValue = CwtConfigType.builder("EnumValue", "enums").reference().build {
        icon(ChronicleIcons.Configs.EnumValue)
        prefix("(enum value)")
        description(ChronicleBundle.message("config.description.enumValue"))
    }
    /**
     * 复杂枚举规则。
     *
     * 路径定位：
     * - `enums/complex_enum[{name}]`。其中 `{name}` 匹配规则名称（即枚举名）。
     *
     * @see CwtComplexEnumConfig
     */
    val ComplexEnum = CwtConfigType.builder("ComplexEnum").build {
        icon(ChronicleIcons.Configs.ComplexEnum)
        prefix("(complex enum)")
        description(ChronicleBundle.message("config.description.complexEnum"))
    }
    /**
     * 并集规则。
     *
     * 路径定位：
     * - `unions/union[{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtUnionConfig
     */
    val Union = CwtConfigType.builder("Union").reference().build {
        icon(ChronicleIcons.Configs.Union)
        prefix("(union)")
        description(ChronicleBundle.message("config.description.union"))
    }
    /**
     * 并集值规则。
     *
     * 路径定位：
     * - `unions/union[{union}]/-`。其中 {union} 匹配并集名。
     *
     * @see CwtUnionConfig
     */
    val UnionValue = CwtConfigType.builder("UnionValue", "unions").reference().build {
        icon(ChronicleIcons.Configs.UnionValue)
        prefix("(union value)")
        description(ChronicleBundle.message("config.description.unionValue"))
    }
    /**
     * 动态值类型规则。
     *
     * 路径定位：
     * - `values/value[{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtDynamicValueTypeConfig
     */
    val DynamicValueType = CwtConfigType.builder("DynamicValueType").build {
        icon(ChronicleIcons.Configs.DynamicValueType)
        prefix("(dynamic value type)")
        description(ChronicleBundle.message("config.description.dynamicValueType"))
    }
    /**
     * 动态值规则。
     *
     * 路径定位：
     * - `values/value[{type}]/-`。其中 `{type}` 匹配动态值类型名。
     *
     * @see CwtDynamicValueTypeConfig
     *  */
    val DynamicValue = CwtConfigType.builder("DynamicValue", "values").reference().build {
        icon(ChronicleIcons.Configs.DynamicValue)
        prefix("(dynamic value)")
        description(ChronicleBundle.message("config.description.dynamicValue"))
    }
    /**
     * 别名规则。
     *
     * 路径定位：
     * - `alias[{name}:{subName}]`。其中 `{name}` 匹配名称，`{subName}`匹配子名（受限支持的数据表达式）。
     *
     * 如果别名的名称为 `modifier`、`trigger` 或 `effect`，将会分别解析为 [Modifier]、[Trigger]、[Effect]。
     *
     * @see CwtAliasConfig
     */
    val Alias = CwtConfigType.builder("Alias").build {
        icon(ChronicleIcons.Configs.Alias)
        prefix("(alias)")
        description(ChronicleBundle.message("config.description.alias"))
    }
    /**
     * 单别名规则。
     *
     * 路径定位：
     * - `single_alias[{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtSingleAliasConfig
     */
    val SingleAlias = CwtConfigType.builder("SingleAlias").build {
        icon(ChronicleIcons.Configs.Alias)
        prefix("(single alias)")
        description(ChronicleBundle.message("config.description.singleAlias"))
    }
    /**
     * 宏规则。
     *
     * 路径定位：
     * - `macro[{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtMacroConfig
     */
    val Macro = CwtConfigType.builder("Macro").build {
        icon(ChronicleIcons.Configs.Macro)
        prefix("(macro)")
        description(ChronicleBundle.message("config.description.macro"))
    }
    /**
     * 链接规则。
     *
     * 路径定位：
     * - `links/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtLinkConfig
     */
    val Link = CwtConfigType.builder("Link").reference().build {
        icon(ChronicleIcons.Configs.Link)
        prefix("(link)")
        description(ChronicleBundle.message("config.description.link"))
    }
    /**
     * 本地化链接规则。
     *
     * 路径定位：
     * - `localisation_links/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtLinkConfig
     */
    val LocalisationLink = CwtConfigType.builder("LocalisationLink").reference().build {
        icon(ChronicleIcons.Configs.Link)
        prefix("(localisation link)")
        description(ChronicleBundle.message("config.description.localisationLink"))
    }
    /**
     * 本地化提升规则。
     *
     * 路径定位：
     * - `localisation_promotions/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtLocalisationPromotionConfig
     */
    val LocalisationPromotion = CwtConfigType.builder("LocalisationPromotion").reference().build {
        icon(ChronicleIcons.Configs.LocalisationPromotion)
        prefix("(localisation promotion)")
        description(ChronicleBundle.message("config.description.localisationPromotion"))
    }
    /**
     * 本地化命令规则。
     *
     * 路径定位：
     * - `localisation_commands/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtLocalisationCommandConfig
     *  */
    val LocalisationCommand = CwtConfigType.builder("LocalisationCommand").reference().build {
        icon(ChronicleIcons.Configs.LocalisationCommand)
        prefix("(localisation command)")
        description(ChronicleBundle.message("config.description.localisationCommand"))
    }
    /**
     * 修正分类规则。
     *
     * 路径定位：
     * - `modifier_categories/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtModifierCategoryConfig
     */
    val ModifierCategory = CwtConfigType.builder("ModifierCategory").reference().build {
        icon(ChronicleIcons.Configs.ModifierCategory)
        prefix("(modifier category)")
        description(ChronicleBundle.message("config.description.modifierCategory"))
    }
    /**
     * 修正规则。
     *
     * 路径定位：
     * - `modifiers/{name}`。其中 `{name}` 匹配规则名称。
     * - `types/type[{type}]/modifiers/{name}`。其中 `{type}` 匹配定义类型，`{name}` 匹配规则名称（其中的 `$` 会被替换为 `<{type}>`）。
     * - `types/type[{type}]/modifiers/subtype[{subtype}]/{name}`。其中 `{subtype}` 匹配定义的子类型。
     *
     * @see CwtModifierConfig
     */
    val Modifier = CwtConfigType.builder("Modifier").reference().build {
        icon(ChronicleIcons.Configs.Modifier)
        prefix("(modifier)")
        description(ChronicleBundle.message("config.description.modifier"))
    }
    /**
     * 触发器规则。
     *
     * 路径定位：
     * - `alias[trigger:{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtAliasConfig
     */
    val Trigger = CwtConfigType.builder("Trigger").reference().build {
        icon(ChronicleIcons.Configs.Trigger)
        prefix("(trigger)")
        description(ChronicleBundle.message("config.description.trigger"))
    }
    /**
     * 效果规则。
     *
     * 路径定位：
     * - `alias[effect:{name}]`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtAliasConfig
     */
    val Effect = CwtConfigType.builder("Effect").reference().build {
        icon(ChronicleIcons.Configs.Effect)
        prefix("(effect)")
        description(ChronicleBundle.message("config.description.effect"))
    }
    /**
     * 作用域规则。
     *
     * 路径定位：
     * - `scopes/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtScopeConfig
     */
    val Scope = CwtConfigType.builder("Scope").reference().build {
        icon(ChronicleIcons.Configs.Scope)
        prefix("(scope)")
        description(ChronicleBundle.message("config.description.scope"))
    }
    /**
     * 作用域组规则。
     *
     * 路径定位：
     * - `scope_groups/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtScopeGroupConfig
     */
    val ScopeGroup = CwtConfigType.builder("ScopeGroup").reference().build {
        icon(ChronicleIcons.Configs.ScopeGroup)
        prefix("(scope group)")
        description(ChronicleBundle.message("config.description.scopeGroup"))
    }
    /**
     * 数据库对象类型规则。
     *
     * 路径定位：
     * - `database_object_types/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtDatabaseObjectTypeConfig
     */
    val DatabaseObjectType = CwtConfigType.builder("DatabaseObjectType").reference().build {
        icon(ChronicleIcons.Configs.DatabaseObjectType)
        prefix("(database object type)")
        description(ChronicleBundle.message("config.description.databaseObjectType"))
    }
    /**
     * 系统作用域规则。
     *
     * 路径定位：
     * - `system_scopes/{name}`。其中 `{name}` 匹配系统作用域 ID。
     *
     * @see CwtSystemScopeConfig
     */
    val SystemScope = CwtConfigType.builder("SystemScope").reference().build {
        icon(ChronicleIcons.Configs.SystemScope)
        prefix("(system scope)")
        description(ChronicleBundle.message("config.description.systemScope"))
    }
    /**
     * 语言区域规则。
     *
     * 路径定位：
     * - `locales/{id}`。其中 `{id}` 匹配语言环境 ID。
     *
     * @see CwtLocaleConfig
     */
    val Locale = CwtConfigType.builder("Locale").reference().build {
        icon(ChronicleIcons.Configs.Locale)
        prefix("(locale)")
        description(ChronicleBundle.message("config.description.locale"))
    }

    // endregion

    // region Extended Config Types

    /**
     * 封装变量的扩展规则。
     *
     * 路径定位：
     * - `scripted_variables/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtExtendedScriptedVariableConfig
     */
    val ExtendedScriptedVariable = CwtConfigType.builder("ExtendedScriptedVariable").build {
        icon(ChronicleIcons.Configs.ExtendedScriptedVariable)
        prefix("(scripted variable config)")
    }
    /**
     * 定义的扩展规则。
     *
     * 路径定位：
     * - `definitions/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtExtendedDefinitionConfig
     */
    val ExtendedDefinition = CwtConfigType.builder("ExtendedDefinition").build {
        icon(ChronicleIcons.Configs.ExtendedDefinition)
        prefix("(definition config)")
    }
    /**
     * 游戏规则（game rule）的扩展规则。
     *
     * 路径定位：
     * - `game_rules/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtExtendedGameRuleConfig
     */
    val ExtendedGameRule = CwtConfigType.builder("ExtendedGameRule").build {
        icon(ChronicleIcons.Configs.ExtendedGameRule)
        prefix("(game rule config)")
    }
    /**
     * 动作触发（on action）的扩展规则。
     *
     * 路径定位：
     * - `on_actions/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtExtendedOnActionConfig
     */
    val ExtendedOnAction = CwtConfigType.builder("ExtendedOnAction").build {
        icon(ChronicleIcons.Configs.ExtendedOnAction)
        prefix("(on action config)")
    }
    /**
     * 参数的扩展规则。
     *
     * 路径定位：
     * - `parameters/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtExtendedParameterConfig
     */
    val ExtendedParameter = CwtConfigType.builder("ExtendedParameter").build {
        icon(ChronicleIcons.Configs.ExtendedParameter)
        prefix("(parameter config)")
    }
    /**
     * 复杂枚举值的扩展规则。
     *
     * 路径定位：
     * - `complex_enum_values/{type}/{name}`。其中 `{type}` 匹配枚举名，`{name}` 匹配规则名称。
     *
     * @see CwtExtendedComplexEnumValueConfig
     */
    val ExtendedComplexEnumValue = CwtConfigType.builder("ExtendedComplexEnumValue").build {
        icon(ChronicleIcons.Configs.ExtendedComplexEnumValue)
        prefix("(complex enum value config)")
    }
    /**
     * 动态值的扩展规则。
     *
     * 路径定位：
     * - `dynamic_values/{type}/{name}`。其中 `{type}` 匹配动态值类型，`{name}` 匹配规则名称。
     *
     * @see CwtExtendedDynamicValueConfig
     */
    val ExtendedDynamicValue = CwtConfigType.builder("ExtendedDynamicValue").build {
        icon(ChronicleIcons.Configs.ExtendedDynamicValue)
        prefix("(dynamic value config)")
    }
    /**
     * 内联脚本（inline script）的扩展规则。
     *
     * 路径定位：
     * - `inline_scripts/{name}`。其中 `{name}` 匹配规则名称。
     *
     * @see CwtExtendedInlineScriptConfig
     */
    val ExtendedInlineScript = CwtConfigType.builder("ExtendedInlineScript").build {
        icon(ChronicleIcons.Configs.ExtendedInlineScript)
        prefix("(inline script config)")
    }

    // endregion
}

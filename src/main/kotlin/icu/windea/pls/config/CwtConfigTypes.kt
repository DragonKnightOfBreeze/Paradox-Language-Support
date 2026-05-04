package icu.windea.pls.config

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons

/**
 * 所有预定义的规则类型。
 *
 * 每个规则类型对应规则文件中一种特定的声明结构，由其在规则文件中的路径模式决定。
 *
 * ### 标准规则类型
 *
 * 从标准 CWT 规则文件中解析：
 *
 * - [Type][CwtConfigTypes.Type] - 类型规则，路径：`types / type[*]`
 * - [Subtype][CwtConfigTypes.Subtype] - 子类型规则，路径：`types / type[*] / subtype[*]`
 * - [Row][CwtConfigTypes.Row] - 行规则（用于CSV），路径：`rows / row[*]`
 * - [Enum][CwtConfigTypes.Enum] - 枚举规则，路径：`enums / enum[*]`
 * - [EnumValue][CwtConfigTypes.EnumValue] - 枚举值，路径：`enums / enum[*] / -`（值，非属性）
 * - [ComplexEnum][CwtConfigTypes.ComplexEnum] - 复杂枚举规则，路径：`enums / complex_enum[*]`
 * - [DynamicValueType][CwtConfigTypes.DynamicValueType] - 动态值类型规则，路径：`values / value[*]`
 * - [DynamicValue][CwtConfigTypes.DynamicValue] - 动态值，路径：`values / value[*] / -`（值，非属性）
 * - [SingleAlias][CwtConfigTypes.SingleAlias] - 单别名规则，路径：`single_alias[*]`
 * - [Alias][CwtConfigTypes.Alias] - 别名规则，路径：`alias[*]`
 * - [Directive][CwtConfigTypes.Directive] - 指令规则，路径：`directive[*]`
 * - [Link][CwtConfigTypes.Link] - 链接规则，路径：`links / *`
 * - [LocalisationLink][CwtConfigTypes.LocalisationLink] - 本地化链接规则，路径：`localisation_links / *`
 * - [LocalisationPromotion][CwtConfigTypes.LocalisationPromotion] - 本地化提升规则，路径：`localisation_promotions / *`
 * - [LocalisationCommand][CwtConfigTypes.LocalisationCommand] - 本地化命令规则，路径：`localisation_commands / *`
 * - [ModifierCategory][CwtConfigTypes.ModifierCategory] - 修正分类规则，路径：`modifier_categories / *`
 * - [Modifier][CwtConfigTypes.Modifier] - 修正规则，路径：`modifiers / *` 或 `types / type[*] / modifiers / *` 或 `alias[modifier:*]`
 * - [Trigger][CwtConfigTypes.Trigger] - 触发器规则，路径：`alias[trigger:*]`
 * - [Effect][CwtConfigTypes.Effect] - 效果规则，路径：`alias[effect:*]`
 * - [Scope][CwtConfigTypes.Scope] - 作用域规则，路径：`scopes / *`
 * - [ScopeGroup][CwtConfigTypes.ScopeGroup] - 作用域组规则，路径：`scope_groups / *`
 * - [DatabaseObjectType][CwtConfigTypes.DatabaseObjectType] - 数据库对象类型规则，路径：`database_object_types / *`
 * - [SystemScope][CwtConfigTypes.SystemScope] - 系统作用域规则，路径：`system_scopes / *`
 * - [Locale][CwtConfigTypes.Locale] - 语言区域规则，路径：`locales / *`
 *
 * ### 扩展规则类型
 *
 * 从插件扩展的规则文件中解析（不检查元素是属性还是值）：
 *
 * - [ExtendedScriptedVariable][CwtConfigTypes.ExtendedScriptedVariable] - 封装变量扩展规则，路径：`scripted_variables / *`
 * - [ExtendedDefinition][CwtConfigTypes.ExtendedDefinition] - 定义扩展规则，路径：`definitions / *`
 * - [ExtendedGameRule][CwtConfigTypes.ExtendedGameRule] - 游戏规则扩展规则，路径：`game_rules / *`
 * - [ExtendedOnAction][CwtConfigTypes.ExtendedOnAction] - 动作触发扩展规则，路径：`on_actions / *`
 * - [ExtendedParameter][CwtConfigTypes.ExtendedParameter] - 参数扩展规则，路径：`parameters / *`
 * - [ExtendedComplexEnumValue][CwtConfigTypes.ExtendedComplexEnumValue] - 复杂枚举值扩展规则，路径：`complex_enum_values / * / *`
 * - [ExtendedDynamicValue][CwtConfigTypes.ExtendedDynamicValue] - 动态值扩展规则，路径：`dynamic_values / * / *`
 * - [ExtendedInlineScript][CwtConfigTypes.ExtendedInlineScript] - 内联脚本扩展规则，路径：`inline_scripts / *`
 *
 * @see CwtConfigType
 */
@Suppress("unused")
object CwtConfigTypes {
    // NOTE 2.1.8 偏好使用 lambda 式构建器，而非多行的链式构建器：可通过代码折叠隐藏细节，方便查看

    /** 类型规则。路径：`types / type[*]`。 */
    val Type = CwtConfigType.builder("Type").build {
        icon(PlsIcons.Configs.Type)
        prefix("(type)")
        description(PlsBundle.message("config.description.type"))
    }
    /** 子类型规则。路径：`types / type[*] / subtype[*]`。 */
    val Subtype = CwtConfigType.builder("Subtype").build {
        icon(PlsIcons.Configs.Type)
        prefix("(subtype)")
        description(PlsBundle.message("config.description.subtype"))
    }
    /** 行规则（用于CSV）。路径：`rows / row[*]`。 */
    val Row = CwtConfigType.builder("Row").build {
        icon(PlsIcons.Configs.Row)
        prefix("(row)")
        description(PlsBundle.message("config.description.row"))
    }
    /** 定值命名空间规则。路径：`defines / *`。 */
    val DefineNamespace = CwtConfigType.builder("DefineNamespace").build {
        icon(PlsIcons.Configs.DefineNamespace)
        prefix("(define namespace)")
        description(PlsBundle.message("config.description.defineNamespace"))
    }
    /** 定值变量规则。路径：`defines / * / *`。 */
    val DefineVariable = CwtConfigType.builder("DefineVariable").build {
        icon(PlsIcons.Configs.DefineVariable)
        prefix("(define variable)")
        description(PlsBundle.message("config.description.defineVariable"))
    }
    /** 枚举规则。路径：`enums / enum[*]`。 */
    val Enum = CwtConfigType.builder("Enum").build {
        icon(PlsIcons.Configs.Enum)
        prefix("(enum)")
        description(PlsBundle.message("config.description.enum"))
    }
    /** 枚举值。路径：`enums / enum[*] / -`（值，非属性）。 */
    val EnumValue = CwtConfigType.builder("EnumValue", "enums").reference().build {
        icon(PlsIcons.Configs.EnumValue)
        prefix("(enum value)")
        description(PlsBundle.message("config.description.enumValue"))
    }
    /** 复杂枚举规则。路径：`enums / complex_enum[*]`。 */
    val ComplexEnum = CwtConfigType.builder("ComplexEnum").build {
        icon(PlsIcons.Configs.ComplexEnum)
        prefix("(complex enum)")
        description(PlsBundle.message("config.description.complexEnum"))
    }
    /** 动态值类型规则。路径：`values / value[*]`。 */
    val DynamicValueType = CwtConfigType.builder("DynamicValueType").build {
        icon(PlsIcons.Configs.DynamicValueType)
        prefix("(dynamic value type)")
        description(PlsBundle.message("config.description.dynamicValueType"))
    }
    /** 动态值。路径：`values / value[*] / -`（值，非属性）。 */
    val DynamicValue = CwtConfigType.builder("DynamicValue", "values").reference().build {
        icon(PlsIcons.Configs.DynamicValue)
        prefix("(dynamic value)")
        description(PlsBundle.message("config.description.dynamicValue"))
    }
    /** 单别名规则。路径：`single_alias[*]`。 */
    val SingleAlias = CwtConfigType.builder("SingleAlias").build {
        icon(PlsIcons.Configs.Alias)
        prefix("(single alias)")
        description(PlsBundle.message("config.description.singleAlias"))
    }
    /** 别名规则。路径：`alias[*]`（排除`modifier`、`trigger`、`effect`子类型）。 */
    val Alias = CwtConfigType.builder("Alias").build {
        icon(PlsIcons.Configs.Alias)
        prefix("(alias)")
        description(PlsBundle.message("config.description.alias"))
    }
    /** 指令规则。路径：`directive[*]`。 */
    val Directive = CwtConfigType.builder("Directive").build {
        icon(PlsIcons.Configs.Directive)
        prefix("(directive)")
        description(PlsBundle.message("config.description.directive"))
    }
    /** 链接规则。路径：`links / *`。 */
    val Link = CwtConfigType.builder("Link").reference().build {
        icon(PlsIcons.Configs.Link)
        prefix("(link)")
        description(PlsBundle.message("config.description.link"))
    }
    /** 本地化链接规则。路径：`localisation_links / *`。 */
    val LocalisationLink = CwtConfigType.builder("LocalisationLink").reference().build {
        icon(PlsIcons.Configs.Link)
        prefix("(localisation link)")
        description(PlsBundle.message("config.description.localisationLink"))
    }
    /** 本地化提升规则。路径：`localisation_promotions / *`。 */
    val LocalisationPromotion = CwtConfigType.builder("LocalisationPromotion").reference().build {
        icon(PlsIcons.Configs.LocalisationPromotion)
        prefix("(localisation promotion)")
        description(PlsBundle.message("config.description.localisationPromotion"))
    }
    /** 本地化命令规则。路径：`localisation_commands / *`。 */
    val LocalisationCommand = CwtConfigType.builder("LocalisationCommand").reference().build {
        icon(PlsIcons.Configs.LocalisationCommand)
        prefix("(localisation command)")
        description(PlsBundle.message("config.description.localisationCommand"))
    }
    /** 修正分类规则。路径：`modifier_categories / *`。 */
    val ModifierCategory = CwtConfigType.builder("ModifierCategory").reference().build {
        icon(PlsIcons.Configs.ModifierCategory)
        prefix("(modifier category)")
        description(PlsBundle.message("config.description.modifierCategory"))
    }
    /** 修正规则。路径：`modifiers / *` 或 `types / type[*] / modifiers / *` 或 `alias[modifier:*]`。 */
    val Modifier = CwtConfigType.builder("Modifier").reference().build {
        icon(PlsIcons.Configs.Modifier)
        prefix("(modifier)")
        description(PlsBundle.message("config.description.modifier"))
    }
    /** 触发器规则。路径：`alias[trigger:*]`。 */
    val Trigger = CwtConfigType.builder("Trigger").reference().build {
        icon(PlsIcons.Configs.Trigger)
        prefix("(trigger)")
        description(PlsBundle.message("config.description.trigger"))
    }
    /** 效果规则。路径：`alias[effect:*]`。 */
    val Effect = CwtConfigType.builder("Effect").reference().build {
        icon(PlsIcons.Configs.Effect)
        prefix("(effect)")
        description(PlsBundle.message("config.description.effect"))
    }
    /** 作用域规则。路径：`scopes / *`。 */
    val Scope = CwtConfigType.builder("Scope").reference().build {
        icon(PlsIcons.Configs.Scope)
        prefix("(scope)")
        description(PlsBundle.message("config.description.scope"))
    }
    /** 作用域组规则。路径：`scope_groups / *`。 */
    val ScopeGroup = CwtConfigType.builder("ScopeGroup").reference().build {
        icon(PlsIcons.Configs.ScopeGroup)
        prefix("(scope group)")
        description(PlsBundle.message("config.description.scopeGroup"))
    }
    /** 数据库对象类型规则。路径：`database_object_types / *`。 */
    val DatabaseObjectType = CwtConfigType.builder("DatabaseObjectType").reference().build {
        icon(PlsIcons.Configs.DatabaseObjectType)
        prefix("(database object type)")
        description(PlsBundle.message("config.description.databaseObjectType"))
    }
    /** 系统作用域规则。路径：`system_scopes / *`。 */
    val SystemScope = CwtConfigType.builder("SystemScope").reference().build {
        icon(PlsIcons.Configs.SystemScope)
        prefix("(system scope)")
        description(PlsBundle.message("config.description.systemScope"))
    }
    /** 语言区域规则。路径：`locales / *`。 */
    val Locale = CwtConfigType.builder("Locale").reference().build {
        icon(PlsIcons.Configs.Locale)
        prefix("(locale)")
        description(PlsBundle.message("config.description.locale"))
    }

    /** 封装变量扩展规则。路径：`scripted_variables / *`。 */
    val ExtendedScriptedVariable = CwtConfigType.builder("ExtendedScriptedVariable").build {
        icon(PlsIcons.Configs.ExtendedScriptedVariable)
        prefix("(scripted variable config)")
    }
    /** 定义扩展规则。路径：`definitions / *`。 */
    val ExtendedDefinition = CwtConfigType.builder("ExtendedDefinition").build {
        icon(PlsIcons.Configs.ExtendedDefinition)
        prefix("(definition config)")
    }
    /** 游戏规则扩展规则。路径：`game_rules / *`。 */
    val ExtendedGameRule = CwtConfigType.builder("ExtendedGameRule").build {
        icon(PlsIcons.Configs.ExtendedGameRule)
        prefix("(game rule config)")
    }
    /** 动作触发扩展规则。路径：`on_actions / *`。 */
    val ExtendedOnAction = CwtConfigType.builder("ExtendedOnAction").build {
        icon(PlsIcons.Configs.ExtendedOnAction)
        prefix("(on action config)")
    }
    /** 参数扩展规则。路径：`parameters / *`。 */
    val ExtendedParameter = CwtConfigType.builder("ExtendedParameter").build {
        icon(PlsIcons.Configs.ExtendedParameter)
        prefix("(parameter config)")
    }
    /** 复杂枚举值扩展规则。路径：`complex_enum_values / * / *`。 */
    val ExtendedComplexEnumValue = CwtConfigType.builder("ExtendedComplexEnumValue").build {
        icon(PlsIcons.Configs.ExtendedComplexEnumValue)
        prefix("(complex enum value config)")
    }
    /** 动态值扩展规则。路径：`dynamic_values / * / *`。 */
    val ExtendedDynamicValue = CwtConfigType.builder("ExtendedDynamicValue").build {
        icon(PlsIcons.Configs.ExtendedDynamicValue)
        prefix("(dynamic value config)")
    }
    /** 内联脚本扩展规则。路径：`inline_scripts / *`。 */
    val ExtendedInlineScript = CwtConfigType.builder("ExtendedInlineScript").build {
        icon(PlsIcons.Configs.ExtendedInlineScript)
        prefix("(inline script config)")
    }
}

package icu.windea.pls.config

import icu.windea.pls.config.CwtDataTypes.AliasMatchLeft
import icu.windea.pls.config.CwtDataTypes.AliasName
import icu.windea.pls.config.CwtDataTypes.Definition
import icu.windea.pls.config.CwtDataTypes.IntValueField
import icu.windea.pls.config.CwtDataTypes.Localisation
import icu.windea.pls.config.CwtDataTypes.ScopeField
import icu.windea.pls.config.CwtDataTypes.SyncedLocalisation
import icu.windea.pls.config.CwtDataTypes.ValueField
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.core.match.AntMatcher
import icu.windea.pls.core.match.RegexMatcher
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.model.ParadoxGameType

/**
 * 所有预定义的数据类型。
 *
 * 每个数据类型对应规则表达式中一种特定的取值形态，决定了规则表达式如何从字符串解析，以及如何与脚本表达式匹配。
 *
 * @see CwtDataType
 * @see CwtDataTypeSets
 */
@Suppress("unused")
object CwtDataTypes {
    // NOTE 2.1.8 偏好使用 lambda 式构建器，而非多行的链式构建器：可通过代码折叠隐藏细节，方便查看

    // Base Data Types

    /**
     * 任意类型。
     *
     * 匹配任意脚本表达式，作为最低优先级的后备匹配。
     *
     * 对应的数据表达式的格式：
     * - `$any`
     */
    val Any = CwtDataType.builder("Any").build {
        withPriority(1.0) // very low
    }
    /**
     * 布尔类型。
     *
     * 匹配布尔值（`yes` / `no`）。
     *
     * 对应的数据表达式的格式：
     * - `bool`
     */
    val Bool = CwtDataType.builder("Bool").build {
        withPriority(100.0) // highest
    }
    /**
     * 整数类型。
     *
     * 匹配整数值。
     * 带范围参数时，还会限制数值范围（仅作检查，仍然视为匹配）。
     * 用引号括起的数字也视为匹配（兼容原版游戏文件）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * 对应的数据表达式的格式：
     * - `int`
     * - `int{range}` - 其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。
     *
     * @see IntRangeInfo
     */
    val Int = CwtDataType.builder("Int").build {
        withPriority(90.0) // very high
    }
    /**
     * 浮点数类型。
     *
     * 匹配浮点数值。
     * 带范围参数时，还会限制数值范围（仅作检查，仍然视为匹配）。
     * 用引号括起的数字也视为匹配（兼容原版游戏文件）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * 对应的数据表达式的格式：
     * - `float`
     * - `float{range}` - 其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。
     *
     * @see FloatRangeInfo
     */
    val Float = CwtDataType.builder("Float").build {
        withPriority(90.0) // very high
    }
    /**
     * 标量类型。
     *
     * 匹配大多数非块表达式（字符串、数字、布尔值等），作为低优先级的宽泛匹配。
     * 作为键时总是匹配。`wildcard_scalar` 变体会设置通配符标记。
     *
     * 对应的数据表达式的格式：
     * - `scalar`
     * - `wildcard_scalar` - 通配符变体。
     */
    val Scalar = CwtDataType.builder("Scalar").build {
        withPriority(2.0) // very low
    }
    /**
     * 颜色字段类型。
     *
     * 匹配脚本颜色字段（如 `rgb { 255 255 255 }`）。
     * 带参数时，还会验证颜色类型前缀。
     *
     * 对应的数据表达式的格式：
     * - `colour_field` `color_field`
     * - `colour[{type}]` `color[{type}]` - 其中 `{type}` 匹配颜色类型（可选值：`rgb` `hsv` `hsv360`）。
     */
    val ColorField = CwtDataType.builder("ColorField").build {
        withPriority(90.0) // very high
    }

    /**
     * 块类型。
     *
     * 匹配脚本块（`{ ... }`）。仅适用于作为值的脚本表达式，并递归匹配块内容。
     *
     * 仅用于内部表示，不对应规则表达式字符串。
     */
    val Block = CwtDataType.builder("Block").build {
        withPriority(100.0) // highest
    }

    // Extended Base Data Types

    /**
     * 百分比字段类型。
     *
     * 匹配数字部分为浮点数的百分比值字符串（如 `50.0%`）。
     *
     * 对应的数据表达式的格式：
     * - `percentage_field`
     */
    val PercentageField = CwtDataType.builder("PercentageField").build {
        withPriority(90.0) // very high
    }
    /**
     * 整数百分比字段类型。
     *
     * 匹配数字部分为整数的百分比值字符串（如 `50%`）。
     *
     * 对应的数据表达式的格式：
     * - `int_percentage_field`
     */
    val IntPercentageField = CwtDataType.builder("IntPercentageField").build {
        withPriority(90.0) // very high
    }
    /**
     * 日期字段类型。
     *
     * 匹配日期值字符串（如 `2200.1.1`）。带参数时还会验证日期格式。
     *
     * 对应的数据表达式的格式：
     * - `date_field`
     * - `date_field[{format}]` - 其中 `{format}` 匹配日期格式（如 `y.M.d`）。
     */
    val DateField = CwtDataType.builder("DateField").build {
        withPriority(90.0)
    }

    // Complex Data Types

    /**
     * 定义引用类型。
     *
     * 匹配对指定类型定义的引用。表达式须为合法标识符（允许 `.` 和 `-`），
     * 可以是整数或浮点数（如 `<technology_tier>` 的情况）。
     * 匹配时验证引用的定义是否存在。
     *
     * 对应的数据表达式的格式：
     * - `<{type}>` - 其中 `{type}` 匹配类型名。
     * - `<{type}.{subtypes}>` - 其中 `{type}` 匹配类型名， `{subtypes}` 匹配点号分隔的一组子类型名。
     *
     * 对应的数据表达式的示例：
     * - `<building>` - 匹配建筑名称引用。
     * - `<event>` - 匹配事件ID引用。
     * - `<event.country>` - 匹配事件ID引用。
     * - `<technology_tier>` - 匹配科技级别引用。这是整数而非字符串。
     */
    val Definition = CwtDataType.builder("Definition").reference().build {
        withPriority(70.0)
    }
    /**
     * 本地化引用类型。
     *
     * 匹配对本地化键的引用。表达式须为合法标识符（允许 `.`、`-`、`'`）。
     * 匹配时验证引用的本地化是否存在。
     * 引用的本地化所在的本地化文件需要位于 `localisation` 或 `localization` 目录（或其子目录）中。
     *
     * 对应的数据表达式的格式：
     * - `localisation`
     */
    val Localisation = CwtDataType.builder("Localisation").reference().build {
        withPriority(60.0)
    }
    /**
     * 同步本地化引用类型。
     *
     * 与 [Localisation] 类似，但指向同步本地化键。
     * 引用的本地化所在的本地化文件需要位于 `localisation_synced` 或 `localization_synced` 目录（或其子目录）中。
     *
     * 对应的数据表达式的格式：
     * - `localisation_synced`
     */
    val SyncedLocalisation = CwtDataType.builder("SyncedLocalisation").reference().build {
        withPriority(60.0)
    }
    /**
     * 内联本地化引用类型。
     *
     * 匹配本地化键引用或用引号括起的任意字符串（后者作为内联文本，以后备匹配返回）。
     *
     * 对应的数据表达式的格式：
     * - `localisation_inline`
     */
    val InlineLocalisation = CwtDataType.builder("InlineLocalisation").reference().build {
        withPriority(60.0)
    }
    /**
     * 修正引用类型。
     *
     * 匹配对修正（modifier）的引用。表达式须为合法标识符。
     * 匹配时验证引用的修正是否在规则组中存在。优先级高于 [Definition]。
     *
     * 对应的数据表达式的格式：
     * - `<modifier>`
     */
    val Modifier = CwtDataType.builder("Modifier").reference().build {
        withPriority(75.0) // higher than Definition
    }

    /**
     * 枚举值类型。
     *
     * 匹配对枚举值的引用。
     * 匹配简单枚举时精确匹配枚举值列表，匹配复杂枚举时则通过索引查询。
     *
     * 对应的数据表达式的格式：
     * - `enum[{name}]` - 其中 `{name}` 匹配枚举名。
     */
    val EnumValue = CwtDataType.builder("EnumValue").reference().build {
        withPriority { configExpression, configGroup ->
            val enumName = configExpression.value ?: return@withPriority 0.0 // unexpected
            if (configGroup.enums.containsKey(enumName)) return@withPriority 80.0
            if (configGroup.complexEnums.containsKey(enumName)) return@withPriority 50.0
            0.0 // unexpected
        }
    }

    /**
     * 动态值读取类型。
     *
     * 匹配动态值表达式（如 `target` `target@root` `target@root.owner`），表示对已声明动态值的读取引用。
     * 动态值的名字须为合法标识符（允许 `.`）。
     *
     * 对应的数据表达式的格式：
     * - `value[{name}]` - 其中 `{name}` 匹配动态值类型。
     *
     * @see ParadoxDynamicValueExpression
     */
    val Value = CwtDataType.builder("Value").reference().build {
        withPriority(40.0)
    }
    /**
     * 动态值写入类型。
     *
     * 匹配动态值表达式（如 `target` `target@root` `target@root.owner`），表示对动态值的写入（声明）引用。
     * 动态值的名字须为合法标识符（允许 `.`）。
     *
     * 对应的数据表达式的格式：
     * - `value_set[{name}]` - 其中 `{name}` 匹配动态值类型。
     *
     * @see ParadoxDynamicValueExpression
     */
    val ValueSet = CwtDataType.builder("ValueSet").reference().build {
        withPriority(40.0)
    }
    /**
     * 动态值类型。
     *
     * 匹配动态值表达式（如 `target` `target@root` `target@root.owner`），表示对动态值的引用（不区分读写）。
     * 动态值的名字须为合法标识符（允许 `.`）。
     *
     * 对应的数据表达式的格式：
     * - `dynamic_value[{name}]` - 其中 `{name}` 匹配动态值类型。
     *
     * @see ParadoxDynamicValueExpression
     */
    val DynamicValue = CwtDataType.builder("DynamicValue").reference().build {
        withPriority(40.0)
    }

    /**
     * 作用域字段类型。
     *
     * 匹配作用域字段表达式（由多个作用域节点组成，通过点号分隔并形成链接，如 `root` `root.owner` `root.event_target:target`）。
     *
     * 对应的数据表达式的格式：
     * - `scope_field`
     *
     * @see ParadoxScopeFieldExpression
     * @see CwtScopeConfig
     */
    val ScopeField = CwtDataType.builder("ScopeField").reference().build {
        withPriority(50.0)
    }
    /**
     * 作用域类型。
     *
     * 匹配作用域字段表达式（由多个作用域节点组成，通过点号分隔并形成链接，如 `root` `root.owner` `root.event_target:target`），同时约束输出作用域类型。
     * 参数为 `any` 时，等同于 [ScopeField]。
     *
     * 对应的数据表达式的格式：
     * - `scope[{type}]` - 其中 `{type}` 匹配作用域类型名。使用 `any` 表示任意类型。
     *
     * @see ParadoxScopeFieldExpression
     * @see CwtScopeConfig
     */
    val Scope = CwtDataType.builder("Scope").reference().build {
        withPriority(50.0)
    }
    /**
     * 作用域组类型。
     *
     * 匹配作用域字段表达式（由多个作用域节点组成，通过点号分隔并形成链接，如 `root` `root.owner` `root.event_target:target`），同时约束输出作用域属于指定的作用域组。
     *
     * 对应的数据表达式的格式：
     * - `scope_group[{name}]` - 其中 `{name}` 匹配作用域分组名。
     *
     * @see ParadoxScopeFieldExpression
     * @see CwtScopeGroupConfig
     */
    val ScopeGroup = CwtDataType.builder("ScopeGroup").reference().build {
        withPriority(50.0)
    }

    /**
     * 值字段类型。
     *
     * 匹配浮点数或值字段表达式（由零个或多个作用域节点，以及最后一个值字段节点组成，通过点号分隔并形成链接，如 `var` `root.var` `root.value:sv`）。
     * 带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * 对应的数据表达式的格式：
     * - `value_field`
     * - `value_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。
     *
     * @see ParadoxValueFieldExpression
     * @see FloatRangeInfo
     */
    val ValueField = CwtDataType.builder("ValueField").reference().build {
        withPriority(45.0)
    }
    /**
     * 整数值字段类型。
     *
     * 匹配整数或整数值字段表达式（由零个或多个作用域节点，以及最后一个值字段节点组成，通过点号分隔并形成链接，如 `var` `root.var` `root.value:sv`）。
     * 带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * 对应的数据表达式的格式：
     * - `int_value_field`
     * - `int_value_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。
     *
     * @see ParadoxValueFieldExpression
     * @see IntRangeInfo
     */
    val IntValueField = CwtDataType.builder("IntValueField").reference().build {
        withPriority(45.0)
    }

    /**
     * 变量字段类型。
     *
     * 匹配浮点数或变量字段表达式（由零个或多个作用域节点，以及最后一个变量节点组成，通过点号分隔并形成链接，如 `var` `root.var`）。
     * 可以视为 [ValueField] 的一种特殊的子集。
     * 带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * 对应的数据表达式的格式：
     * - `variable_field`
     * - `variable_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。
     * - `variable_field_32` - 32 位变体。
     * - `variable_field_32{range}` - 32 位变体。其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。
     *
     * @see ParadoxVariableFieldExpression
     * @see FloatRangeInfo
     */
    val VariableField = CwtDataType.builder("VariableField").reference().build {
        withPriority(45.0)
    }
    /**
     * 整数变量字段类型。
     *
     * 匹配整数或整数变量字段表达式（由零个或多个作用域节点，以及最后一个变量节点组成，通过点号分隔并形成链接，如 `var` `root.var`）。
     * 可以视为 [IntValueField] 的一种特殊的子集。
     * 带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。
     *
     * 范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。
     *
     * 对应的数据表达式的格式：
     * - `int_variable_field`
     * - `int_variable_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。
     * - `int_variable_field_32` - 32 位变体。
     * - `int_variable_field_32{range}` - 32 位变体。其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。
     *
     * @see ParadoxVariableFieldExpression
     * @see IntRangeInfo
     */
    val IntVariableField = CwtDataType.builder("IntVariableField").reference().build {
        withPriority(45.0)
    }

    /**
     * 命令表达式类型。
     *
     * 匹配命令表达式（由零个或多个命令作用域节点，以及最后一个命令字段节点组成，通过点号分隔并形成链接，如 `GetName` `Root.GetName`）。
     * 命令表达式在本地化文件中被广泛使用（`[...]`），然而，目前仅作占位，不支持匹配脚本文件中的表达式。
     *
     * 对应的数据表达式的格式：
     * - `$command`
     *
     * @see ParadoxCommandExpression
     * @since 2.1.1
     */
    val Command = CwtDataType.builder("Command").reference().build {
        withPriority(45.0)
    }
    /**
     * 定值引用表达式类型。
     *
     * 匹配定值引用表达式（如 `define:Namespace|Variable`）。
     *
     * 对应的数据表达式的格式：
     * - `$define_reference`
     *
     * @see ParadoxDefineReferenceExpression
     * @since 1.3.25
     */
    val DefineReference = CwtDataType.builder("DefineReference").reference().build {
        withPriority(60.0)
    }
    /**
     * 数组定值引用表达式类型。
     *
     * 匹配数组定值引用表达式（如 `array_define:Namespace|Variable|0`）。
     *
     * 对应的数据表达式的格式：
     * - `$array_define_reference`
     *
     * （TODO 2.1.10 待实现）
     *
     * @see ParadoxArrayDefineReferenceExpression
     * @since 2.1.10
     */
    val ArrayDefineReference = CwtDataType.builder("ArrayDefineReference").reference().build {
        withPriority(60.0)
    }
    /**
     * 动态值集合表达式类型。
     *
     * 匹配动态值集合表达式（由逗号分隔的一组动态值节点组成，如 `flag` `flag1,flag2`）。
     * 在条件变体下，可对其中的动态值节点进行取反（如 `flag1,not(flag2)`）。
     *
     * 对应的数据表达式的格式：
     * - `$dynamic_value_set[{name}]` - 其中 `{name}` 匹配动态值类型。
     * - `$dynamic_value_set_condition[{name}]` - 条件变体。其中 `{name}` 匹配动态值类型。
     *
     * （TODO 2.1.10 待实现）
     *
     * @see ParadoxDynamicValueSetExpression
     * @since 2.1.10
     */
    val DynamicValueSet = CwtDataType.builder("DynamicValueSet").reference().build {
        withPriority(60.0)
    }
    /**
     * 数据库对象表达式类型。
     *
     * 匹配数据库对象表达式（由冒号分隔的多段引用节点组成，如 `building:x` `civic:x:y`）。
     *
     * 对应的数据表达式的格式：
     * - `$database_object`
     *
     * @see ParadoxDatabaseObjectExpression
     * @since 1.3.9
     */
    val DatabaseObject = CwtDataType.builder("DatabaseObject").reference().build {
        withPriority(60.0)
    }
    /**
     * 命名格式表达式类型。
     *
     * 匹配命名格式表达式（如 `{alpha}` `{<adj> {<noun>}}`）。
     *
     * 对应的数据表达式的格式：
     * - `name_format[{type}]`
     *
     * @see ParadoxNameFormatExpression
     */
    val NameFormat = CwtDataType.builder("NameFormat").reference().build {
        withPriority(60.0)
    }

    /**
     * 着色器效果类型。
     *
     * 匹配对着色器效果（shader effect）的引用。
     * 插件目前将这些引用视为动态引用，尽管其声明实际上位于 `.shader` 文件中。
     *
     * “动态引用”意味着不存在实际上的声明处，仅区分读写访问，如同动态值一样。而这里仅总是视为读访问。
     *
     * 对应的数据表达式的格式：
     * - `$shader_effect`
     */
    val ShaderEffect = CwtDataType.builder("ShaderEffect").reference().build {
        withPriority(30.0)
    }

    /**
     * 网格定位器类型。
     *
     * 匹配对网格定位器（mesh locator）的引用。
     * 插件目前将这些引用视为动态引用，尽管其声明实际上位于 `.mesh` 文件中。
     *
     * “动态引用”意味着不存在实际上的声明处，仅区分读写访问，如同动态值一样。而这里仅总是视为读访问。
     *
     * 对应的数据表达式的格式：
     * - `$mesh_locator`
     */
    val MeshLocator = CwtDataType.builder("MeshLocator").reference().build {
        withPriority(30.0)
    }

    /**
     * 带等级的科技类型。
     *
     * 匹配带等级科技引用（如 `some_repeatable_tech@1`），通过 `@` 分隔科技名和等级。
     * 仅限 Stellaris 游戏类型，且优先级低于 [Definition]。
     *
     * 对应的数据表达式的格式：
     * - `$technology_with_level`
     */
    @WithGameType(ParadoxGameType.Stellaris)
    val TechnologyWithLevel = CwtDataType.builder("TechnologyWithLevel").reference().build {
        withPriority(69.0) // lower than Definition
    }

    /**
     * 参数名类型。
     *
     * 匹配参数名。表达式须为合法标识符。即使对应的定义声明中不存在该参数名，也视为匹配。
     *
     * 对应的数据表达式的格式：
     * - `$parameter`
     */
    val Parameter = CwtDataType.builder("Parameter").reference().build {
        withPriority(10.0)
    }
    /**
     * 参数值类型。
     *
     * 匹配参数值。只要不是块即可匹配。
     *
     * 对应的数据表达式的格式：
     * - `$parameter_value`
     */
    val ParameterValue = CwtDataType.builder("ParameterValue").reference().build {
        withPriority(90.0) // same to Scalar
    }
    /**
     * 本地化参数名类型。
     *
     * 匹配本地化参数名。表达式须为合法标识符（允许 `.`、`-`、`'`）。
     *
     * 对应的数据表达式的格式：
     * - `$localisation_parameter`
     */
    val LocalisationParameter = CwtDataType.builder("LocalisationParameter").reference().build {
        withPriority(10.0)
    }

    // Alias Data Types

    /**
     * 单别名右侧类型。
     *
     * 不直接参与脚本匹配，由别名解析机制处理。只能用来匹配属性值。
     *
     * 对应的数据表达式的格式：
     * - `single_alias_right[{name}]` - 其中 `{name}` 匹配单别名的名字。
     */
    val SingleAliasRight = CwtDataType.builder("SingleAliasRight").reference().build()
    /**
     * 别名键字段类型。
     *
     * 匹配时解析别名子键并递归匹配。
     *
     * 对应的数据表达式的格式：
     * - `alias_keys_field[{name}]` - 其中 `{name}` 匹配别名的名字。
     */
    val AliasKeysField = CwtDataType.builder("AliasKeysField").reference().build()
    /**
     * 别名名称类型。
     *
     * 匹配时解析别名子键并递归匹配。只能用来匹配属性键，且需要与 [AliasMatchLeft] 组合使用。
     *
     * 对应的数据表达式的格式：
     * - `alias_name[{name}]` - 其中 `{name}` 匹配别名的名字。
     */
    val AliasName = CwtDataType.builder("AliasName").reference().build()
    /**
     * 别名匹配左侧类型。
     *
     * 不直接参与脚本匹配，由别名解析机制处理。只能用来匹配属性值，且需要与 [AliasName] 组合使用。
     *
     * 对应的数据表达式的格式：
     * - `alias_match_left[{name}]` - 其中 `{name}` 匹配别名的名字。
     */
    val AliasMatchLeft = CwtDataType.builder("AliasMatchLeft").reference().build()

    // Path Reference Data Types

    /**
     * 绝对文件路径类型。
     *
     * 匹配绝对文件路径字符串。
     * 匹配时仅验证为字符串类型（通配匹配）。
     *
     * 对应的数据表达式的格式：
     * - `abs_filepath`
     */
    val AbsoluteFilePath = CwtDataType.builder("AbsoluteFilePath").reference().build {
        withPriority(70.0)
    }
    /**
     * 图标路径类型。
     *
     * 匹配对图标文件的路径引用。
     * 匹配时验证路径引用的图片文件是否存在，需要指定路径模式，从而限定父路径。不区分文件扩展名。
     *
     * 对应的数据表达式的格式：
     * - `icon[{path}]` - 其中 `{path}` 匹配路径模式（如 `gfx/interface/icons`）。
     */
    val Icon = CwtDataType.builder("Icon").reference().build {
        withPriority(70.0)
    }
    /**
     * 文件路径类型。
     *
     * 匹配对文件的路径引用。
     * 匹配时验证路径引用的文件是否存在，可以指定路径模式，从而限定父路径和文件扩展名，或是使用相对路径定位。
     *
     * 对应的数据表达式的格式：
     * - `filepath` - 使用相对于入口路径的路径定位。
     * - `filepath[./]` - 使用相当于当前脚本文件的路径定位。
     * - `filepath[{path}]` - 其中 `{path}` 匹配路径模式。
     */
    val FilePath = CwtDataType.builder("FilePath").reference().build {
        withPriority(70.0)
    }
    /**
     * 文件名类型。
     *
     * 匹配对文件名的引用。
     * 匹配时验证路径引用的文件是否存在。可以指定路径模式，从而限定父路径。仅区分文件名。
     *
     * 对应的数据表达式的格式：
     * - `filename`
     * - `filename[{path}]` - 其中 `{path}` 匹配路径模式。
     */
    val FileName = CwtDataType.builder("FileName").reference().build {
        withPriority(70.0)
    }

    // Pattern Aware Data Types

    /**
     * 常量类型。
     *
     * 匹配与常量值完全相同的表达式。
     * 作为值时，布尔常量（`yes` / `no`）不会匹配用引号括起的字符串字面量。
     * 另外，不含特殊字符（`:.@[]<>`）的字符串字面量会被回退解析为此类型。
     *
     * 对应的数据表达式的格式：
     * - 直接使用常量值作为数据表达式字符串本身，如 `yes`、`10`、`trigger` 等。
     */
    val Constant = CwtDataType.builder("Constant").patternAware().build {
        withPriority(100.0) // highest
    }
    /**
     * 模板表达式类型。
     *
     * 由常量文本片段和引用片段交替组成的模式。
     * 匹配时将脚本表达式按模板结构拆分，逐个验证各引用片段。
     *
     * @see ParadoxTemplateExpression
     */
    val TemplateExpression = CwtDataType.builder("TemplateExpression").patternAware().build {
        withPriority(65.0)
    }
    /**
     * ANT 路径模式类型（模式感知）。
     *
     * 匹配符合 ANT 路径模式的表达式。支持通配符 `?`（单字符）、`*`（单段）和 `**`（多段，不常用）。
     *
     * 对应的数据表达式的格式：
     * - `ant:{pattern}` - 其中 `{pattern}` 匹配模式。
     * - `ant.i:{pattern}` - 忽略大小写的变体。
     *
     * @see AntMatcher
     * @since 1.3.6
     */
    val Ant = CwtDataType.builder("Ant").patternAware().build()
    /**
     * 正则表达式模式类型（模式感知）。
     *
     * 匹配符合正则表达式的表达式。
     *
     * 对应的数据表达式的格式：
     * - `re:{pattern}` - 其中 `{pattern}` 匹配模式。
     * - `re.i:{pattern}` - 忽略大小写的变体。
     *
     * @see RegexMatcher
     * @since 1.3.6
     */
    val Regex = CwtDataType.builder("Regex").patternAware().build()

    // Suffix Aware Data Types

    // TODO SUFFIX_AWARE 目前不兼容/不支持：代码补全、用法查询

    /**
     * 后缀感知的定义引用类型。
     *
     * 由基础定义引用和逗号分隔的后缀列表组成，匹配时同时验证定义引用和后缀。
     * 如果后缀列表为空，则退化为普通的 [Definition]。
     *
     * 对应的数据表达式的格式：
     * - `<{type}>|{suffixes}` - 其中 `{type}` 匹配类型名，`{suffixes}` 匹配逗号分隔的一组后缀。
     * - `<{type}.{subtypes}>|{suffixes}` - 其中 `{type}` 匹配类型名， `{subtypes}` 匹配点号分隔的一组子类型名，`{suffixes}` 匹配逗号分隔的一组后缀。
     *
     * @since 2.0.5
     */
    val SuffixAwareDefinition = CwtDataType.builder("SuffixAwareDefinition").suffixAware().build() // #162, #193
    /**
     * 后缀感知的本地化引用类型。
     *
     * 由基础本地化引用和逗号分隔的后缀列表组成，匹配时同时验证本地化引用和后缀。
     * 如果后缀列表为空，则退化为普通的 [Localisation]。
     *
     * 对应的数据表达式的格式：
     * - `localisation|{suffixes}` - 其中 `{suffixes}` 匹配逗号分隔的一组后缀。
     *
     * @since 2.0.5
     * @see
     */
    val SuffixAwareLocalisation = CwtDataType.builder("SuffixAwareLocalisation").suffixAware().build() // #162, #193
    /**
     * 后缀感知的同步本地化引用类型。
     *
     * 由基础同步本地化引用和逗号分隔的后缀列表组成，匹配时同时验证同步本地化引用和后缀。
     * 如果后缀列表为空，则退化为普通的 [SyncedLocalisation]。
     *
     * 对应的数据表达式的格式：
     * - `localisation_synced|{suffixes}` - 其中 `{suffixes}` 匹配逗号分隔的一组后缀。
     *
     * @since 2.0.5
     */
    val SuffixAwareSyncedLocalisation = CwtDataType.builder("SuffixAwareLocalisationSynced").suffixAware().build() // #162, #193
}

package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.StellarisNameFormatExpressionResolverImpl

/**
 * Stellaris 命名格式表达式。
 *
 * 说明：
 * - 用于解析 Stellaris 中以花括号包裹的“命名格式模板”，内部可混合“定义占位”“命令表达式”“本地化标识符”与嵌套参数块。
 * - 对应的规则数据类型为 [CwtDataTypes.StellarisNameFormat]。
 *
 * 示例：
 * ```
 * {<eater_adj> {<patron_noun>}}
 * {AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}
 * {alpha}
 * ```
 *
 * 语法：
 * ```bnf
 * name_format_expression ::= name_format_closure
 * name_format_closure ::= "{" closure_content? "}"
 * closure_content ::= closure_item*
 * private closure_item ::= name_format_closure | command | name_part | name_format_localisation | name_format_text
 * name_part ::= "<" name_format_definition ">"
 * command ::= "[" command_expression "]"
 * ```
 *
 * ### 语法与结构
 *
 * - 整体形态：
 *   - 顶层仅识别由花括号包裹的“闭包段”`{...}`。若字符串中不含任何`{`，则非空白部分整体被视为错误片段，其前后空白会作为空白节点保留。
 *
 * - 闭包段的内容由一系列节点顺序拼接而成，空白会被保留为空白节点。按优先级识别如下节点类型：
 *   1) 定义占位：形如`<name>`。
 *      - 解析为`StellarisNamePartNode`，其内部包含`"<"`与`">"`标记节点，以及一个`StellarisNameFormatDefinitionNode`。
 *      - `name` 的类型由规则中的 `formatName` 推导为 `${formatName}_name_parts_list`，用于跨文件的定义名解析与跳转。
 *   2) 命令表达式：形如`[ ... ]`。
 *      - 解析为`ParadoxCommandNode`，内部为`ParadoxCommandExpression`，并保留方括号标记节点。
 *   3) 本地化标识符：一段看起来是“标识符”的连续字符序列（字母/数字/`_`/`-`/`.`/`'`）。
 *      - 解析为`StellarisNameFormatLocalisationNode`，按偏好语言或上下文语言进行引用解析。
 *   4) 文本：当不匹配上述任一结构时，根据空白进行拆分。
 *      - 非空白部分解析为`StellarisNameFormatTextNode`，空白部分解析为`ParadoxBlankNode`，以保留布局与渲染效果。
 *
 * - 嵌套与不完整输入：
 *   - 闭包可递归嵌套，即`{ ... { ... } ... }`，对应嵌套的`StellarisNameFormatClosureNode`结构。
 *   - 当任一成对标记未闭合（如缺失 `]`、`>`、`}`），解析器会在相应包装节点内部追加空的`ParadoxErrorTokenNode`以标记不完整输入，
 *     并在必要时于当前层末尾补充一个尾随错误节点，便于高亮与补全的容错处理。
 *
 * - 校验约束：
 *   - 定义名需满足参数感知的标识符约束（`isParameterAwareIdentifier()`）。
 *   - 本地化名允许`.`、`-`、`'`，满足`isParameterAwareIdentifier('.', '-', '\'')`的检查。
 *
 * - 配置关联：
 *   - `formatName` 来自 CWT 规则，定义占位的类型固定为 `${formatName}_name_parts_list`；若无法推导类型，相关占位被标记为错误节点。
 *
 * - 典型节点列表（仅列出主要结构）：
 *   - `StellarisNameFormatClosureNode`、`StellarisNamePartNode`、`StellarisNameFormatDefinitionNode`、
 *     `ParadoxCommandNode`、`StellarisNameFormatLocalisationNode`、`StellarisNameFormatTextNode`、
 *     `ParadoxBlankNode`、`ParadoxErrorTokenNode`。
 */
interface StellarisNameFormatExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): StellarisNameFormatExpression?
    }

    companion object : Resolver by StellarisNameFormatExpressionResolverImpl()
}

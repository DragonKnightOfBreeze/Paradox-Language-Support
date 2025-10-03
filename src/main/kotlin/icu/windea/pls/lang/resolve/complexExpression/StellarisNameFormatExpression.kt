package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.StellarisNameFormatExpressionResolverImpl

/**
 * Stellaris 命名格式表达式。
 *
 * 用于解析 Stellaris 中以花括号包裹的“命名格式模板”，内部可混合“定义占位”“命令表达式”“本地化标识符”与嵌套参数块。
 *
 * 说明：
 * - 对应 CWT 数据类型：`CwtDataTypes.StellarisNameFormat`。
 * - 规则值表达式应形如 `stellaris_name_format[x]`，其中 `x` 为“格式名/formatName”。
 * - 解析时需结合传入的规则对象（[config]）获取 `formatName`，并据此推导可被 `<...>` 引用的定义类型：`"${formatName}_name_parts_list"`。
 *
 * 示例：
 * ```paradox_script
 * pre_communications_name_format = "{<eater_adj> {<patron_noun>}}"
 * pre_communications_name_format = "{AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}"
 * pre_communications_name_format = "{$code_name_animator$}"
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 最外层必须且仅有一层花括号：`'{' content '}'`。
 * - `content` 由若干“片段”顺序构成，允许空白并允许嵌套的 `{ ... }` 参数块：
 *   - 常量文本片段：任意非特殊符号文本（保留原样显示）。
 *   - 定义占位片段：`'<' name '>'`，`name` 需解析为类型为 `"${formatName}_name_parts_list"` 的同名定义。
 *   - 命令表达式片段：`'[' command ']'`，整体交由 [ParadoxCommandExpression] 解析。
 *   - 本地化调用片段：`name`，解析为同名本地化；若紧随 `{ ... }`，则视为带参数的调用，`{ ... }` 内继续按本规则解析并作为参数片段（仅语义建模，插件不负责实际渲染）。
 * - 说明：历史上也存在 `$name$` 形式的本地化占位，但该形式在脚本语法中会被解析为参数；目前不兼容此形式。
 *
 * #### 节点组成（建议）
 * - 顶层与任意 `{ ... }`：由分隔标记节点（`'{'`/`'}'`，可用 [ParadoxMarkerNode] 表示）与子片段节点构成。
 * - 片段节点：
 *   - 常量文本：可用 [ParadoxStringLiteralNode] 表示。
 *   - 定义占位：可复用 [ParadoxDataSourceNode]，其 `linkConfigs` 指向定义类型 `"${definitionType}"`；
 *   - 命令表达式：复用 [ParadoxCommandExpression] 作为子表达式；
 *   - 本地化调用：可复用 [ParadoxDataSourceNode] 指向 `Localisation` 链接；其后若带 `{ ... }` 则将花括号内解析结果作为参数子节点。
 *
 * #### 解析链路摘要
 * 1. 从 [config] 的 `configExpression.value` 读取 `formatName`，并计算 `definitionType = "${formatName}_name_parts_list"`。
 * 2. 校验文本：若不以 `'{'` 开始或不以 `'}'` 结束，在非不完整模式下判为不匹配；不完整模式下允许缺失闭合并生成错误节点。
 * 3. 线性扫描顶层 `content`：
 *    - 碰到 `'<'` -> 读取到匹配的 `'>'` 生成“定义占位”片段；
 *    - 碰到 `'['` -> 读取到匹配的 `']'` 交由 [ParadoxCommandExpression] 解析；
 *    - 碰到 `'{'` -> 读取到匹配的 `'}'` 作为“参数块”递归解析；
 *    - 其他字符 -> 聚合为常量文本片段；
 *    - 参数文本与字符串字面量等由已存在的工具（如 `ParadoxExpressionManager.getParameterRanges`）避开切分冲突。
 * 4. 结构完成后执行 DSL 校验与错误收集：未闭合/空片段/不可解析引用等通过 [ParadoxComplexExpressionErrorBuilder] 构造。
 *
 * #### 约束与高亮
 * - `<name>` 需为标识符；引用的定义按 `definitionType` 解析并提供导航/补全与用法查询。
 * - `name` 作为本地化需满足命名规则，并提供导航/补全（优先当前语言，带回退）。
 * - `[...]` 片段沿用命令表达式的解析与高亮规则。
 *
 * @property config 解析所基于的规则对象（包含 `configExpression`）。
 * @property formatName 规则值表达式中的格式名（如 `pre_communications`）。
 * @property definitionType 由 `formatName` 推导的定义类型名（如 `pre_communications_name_parts_list`）。
 *
 * @see icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
 * @see icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
 * @see icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxStringLiteralNode
 * @see icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
 */
interface StellarisNameFormatExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>
    val formatName: String?
    val definitionType: String?

    interface Resolver {
        /**
         * 解析 Stellaris 命名格式表达式。
         *
         * 说明：必须额外传入规则对象 [config] 以获取 `formatName`，从而推导 `definitionType`。
         */
        fun resolve(
            text: String,
            range: TextRange,
            configGroup: CwtConfigGroup,
            config: CwtConfig<*>,
        ): StellarisNameFormatExpression?
    }

    companion object : Resolver by StellarisNameFormatExpressionResolverImpl()
}

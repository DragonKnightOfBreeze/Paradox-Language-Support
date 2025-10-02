package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxDynamicValueExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression

/**
 * 动态值表达式。
 *
 * ### 说明
 * - 对应的规则数据类型为 [CwtDataTypeGroups.DynamicValue]。
 *
 * ### 示例
 * ```
 * some_variable
 * some_variable@root
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 由“动态值名”与可选的 `@` 后缀“作用域字段表达式”组成：`dynamic_value` 或 `dynamic_value@scope_field`。
 * - 文本按第一处 `@` 切分；`@` 之后整体交由 [ParadoxScopeFieldExpression] 解析。
 *
 * #### 节点组成
 * - 动态值名：[ParadoxDynamicValueNode]（首段）。
 * - 分隔符：`@`（[ParadoxMarkerNode]，可选）。
 * - 作用域字段表达式：[ParadoxScopeFieldExpression]（可选）。
 *
 * #### 解析与约束
 * - 动态值名需为参数感知的标识符（兼容 `.`）。
 * - 仅处理第一处 `@`；其后内容整体作为作用域字段表达式输入。
 */
interface ParadoxDynamicValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>

    val dynamicValueNode: ParadoxDynamicValueNode
    val scopeFieldExpression: ParadoxScopeFieldExpression?

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression?
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression?
    }

    companion object : Resolver by ParadoxDynamicValueExpressionResolverImpl()
}

package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxTemplateExpressionResolverImpl
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetNode

/**
 * 模版表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.TemplateExpression]。
 * - 模版由 CWT 规则提供（或由修正的 `template` 字段提供），本表达式文本按模版匹配并被切分为“常量片段/占位片段”。
 *
 * ### 语法
 *
 * ```bnf
 * template_expression ::= snippet+
 *
 * private snippet ::= template_snippet_constant | template_snippet
 * ```
 *
 * ### 语法与结构
 *
 * #### 整体形态
 * - 表达式文本与模版按组匹配，顺序交替产生：常量片段与占位片段。
 * - 允许部分匹配（不完整代码场景）。
 *
 * #### 节点组成
 * - 常量片段：[ParadoxTemplateSnippetConstantNode]（与模版的常量部分对应）。
 * - 占位片段：[ParadoxTemplateSnippetNode]（与模版的引用/占位部分对应）。
 *
 * #### 解析要点
 * - 模版来源：若配置为修正（`CwtModifierConfig`），取其 `template`；否则取 `configExpression` 中的 [CwtDataTypes.TemplateExpression]。
 * - 通过 `toMatchedRegex` 将模版转为正则并对文本进行组匹配，再依组构造片段节点。
 */
interface ParadoxTemplateExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression?
    }

    companion object : Resolver by ParadoxTemplateExpressionResolverImpl()
}

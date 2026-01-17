package icu.windea.pls.lang.resolve.complexExpression.dsl

import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

object ParadoxComplexExpressionDslRenderer {
    data class Options(
        val trimEmptyNodes: Boolean = false,
    )

    /**
     * 将已解析的复杂表达式节点渲染为用于测试的 DSL 代码片段。
     *
     * 规则：
     * - 根节点与任意子节点若为表达式类型，使用 `expression<ExprType>("text", s..e)`；
     * - 普通节点使用 `node<NodeType>("text", s..e)`；
     * - 有子节点时输出块 `{ ... }`，否则省略。
     */
    fun render(node: ParadoxComplexExpressionNode, options: Options = Options()): String {
        val sb = StringBuilder()
        appendNode(sb, node, indent = "", asRoot = true, options)
        return sb.toString()
    }

    private fun appendNode(
        sb: StringBuilder,
        node: ParadoxComplexExpressionNode,
        indent: String,
        asRoot: Boolean,
        options: Options,
    ) {
        // 可选：裁剪空叶子节点（兼容 incomplete=true 可能出现的空节点）
        if (!asRoot && options.trimEmptyNodes && node.text.isEmpty() && node.nodes.isEmpty()) return
        val typeName = resolveTypeName(node)
        val text = escape(node.text)
        val s = node.rangeInExpression.startOffset
        val e = node.rangeInExpression.endOffset
        val children = node.nodes
        val isExpression = node is ParadoxComplexExpression
        val funName = when {
            asRoot -> "buildExpression"
            isExpression -> "expression"
            else -> "node"
        }
        // header
        sb.append(indent)
            .append(funName)
            .append('<').append(typeName).append('>')
            .append('(')
            .append('"').append(text).append('"')
            .append(',').append(' ')
            .append(s).append("..").append(e)
            .append(')')
        if (children.isEmpty()) {
            sb.append('\n')
            return
        }
        sb.append(' ').append('{').append('\n')
        val childIndent = "$indent    "
        for (c in children) {
            appendNode(sb, c, childIndent, asRoot = false, options)
        }
        sb.append(indent).append('}').append('\n')
    }

    private fun resolveTypeName(node: ParadoxComplexExpressionNode): String {
        // 对表达式节点，优先输出接口名，避免 Impl 名称
        return node.javaClass.simpleName.removeSuffix("Impl")
    }

    private fun escape(text: String): String {
        val sb = StringBuilder(text.length + 8)
        for (ch in text) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}

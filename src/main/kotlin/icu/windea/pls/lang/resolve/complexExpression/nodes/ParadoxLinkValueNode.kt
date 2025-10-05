package icu.windea.pls.lang.resolve.complexExpression.nodes

/**
 * 链接类“值节点”的统一接口。
 *
 * 用于显式暴露参数节点列表与基于表达式内偏移量计算的参数索引，
 * 以降低补全侧对具体节点内部结构的耦合。
 */
interface ParadoxLinkValueNode : ParadoxComplexExpressionNode {
    /** 仅包含核心参数节点，排除空白与分隔符等辅助节点。*/
    val argumentNodes: List<ParadoxComplexExpressionNode>

    /**
     * 根据表达式内偏移量计算参数索引（从 0 开始）。
     * 若偏移量位于所有参数之后，则返回最后一个参数的索引；若没有参数则返回 0。
     */
    fun getArgumentIndex(offsetInExpression: Int): Int {
        // 基于之前的逗号的个数
        return nodes.filter { it is ParadoxMarkerNode && it.text == "," }.count { it.rangeInExpression.endOffset <= offsetInExpression }
    }
}

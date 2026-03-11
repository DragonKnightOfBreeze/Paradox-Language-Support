package icu.windea.pls.lang.resolve.complexExpression.nodes

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxLightElementBase

/**
 * 基于动态数据的节点。
 *
 * 解析引用得到的目标可能来自脚本文件、本地化文件，或者作为一个 [ParadoxLightElementBase]。
 * 如果只会来自规则文件，或者不作为决定性的标准，则不应作为一个动态数据节点。
 */
interface ParadoxDynamicDataNode: ParadoxComplexExpressionNode {
    override fun getReference(element: ParadoxExpressionElement): ParadoxIdentifierNode.Reference?
}

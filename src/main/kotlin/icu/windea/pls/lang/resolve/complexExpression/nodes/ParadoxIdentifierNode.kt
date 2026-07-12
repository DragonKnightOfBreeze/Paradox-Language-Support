package icu.windea.pls.lang.resolve.complexExpression.nodes

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxLightElementBase
import icu.windea.pls.lang.references.ParadoxConstrainedPsiReference
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint

/**
 * 作为标识符的节点。
 *
 * 解析引用得到的目标可能来自规则文件、脚本文件、本地化文件，或者作为一个 [ParadoxLightElementBase]。
 */
interface ParadoxIdentifierNode : ParadoxComplexExpressionNode {
    override fun getReference(element: ParadoxExpressionElement): Reference?

    interface Reference : ParadoxConstrainedPsiReference {
        override fun canResolveFor(constraint: ParadoxReferenceConstraint): Boolean {
            return false
        }
    }
}

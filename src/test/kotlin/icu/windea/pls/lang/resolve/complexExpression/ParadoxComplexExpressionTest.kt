package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionTest : BasePlatformTestCase() {
    protected fun ParadoxComplexExpressionNode.check(dslNode: ParadoxComplexExpressionDslNode) {
        return ParadoxComplexExpressionDslChecker.check(this, dslNode)
    }

    protected fun ParadoxComplexExpressionNode.render(): String {
        return ParadoxComplexExpressionDslRenderer.render(this)
    }
}

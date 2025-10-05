package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslChecker
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslNode
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslRender
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionTest : BasePlatformTestCase() {
    protected fun ParadoxComplexExpressionNode.check(dslNode: ParadoxComplexExpressionDslNode) {
        return ParadoxComplexExpressionDslChecker.check(this, dslNode)
    }

    protected fun ParadoxComplexExpressionNode.render(): String {
        return ParadoxComplexExpressionDslRender.render(this)
    }
}

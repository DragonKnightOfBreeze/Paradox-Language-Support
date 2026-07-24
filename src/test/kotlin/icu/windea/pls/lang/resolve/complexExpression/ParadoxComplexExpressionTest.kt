package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.core.withState
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.test.ChronicleTestScope

/**
 * @see ParadoxComplexExpression
 */
abstract class ParadoxComplexExpressionTest : BasePlatformTestCase(), ChronicleTestScope {
    protected inline fun <T> markIncomplete(incomplete: Boolean, action: () -> T): T {
        if (!incomplete) return action()
        return withState(ChronicleThreadContext.incompleteComplexExpression, action)
    }

    protected fun ParadoxComplexExpressionNode.check(dslNode: ParadoxComplexExpressionDsl) {
        return ParadoxComplexExpressionDslChecker.check(this, dslNode)
    }

    protected fun ParadoxComplexExpressionNode.render(): String {
        return ParadoxComplexExpressionDslRenderer.render(this)
    }

    protected fun ParadoxComplexExpressionNode.renderAndPrintln() {
        println(ParadoxComplexExpressionDslRenderer.render(this))
    }
}

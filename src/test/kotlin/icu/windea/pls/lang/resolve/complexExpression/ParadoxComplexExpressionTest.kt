package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslChecker
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslNode
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslRender
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.runBlocking

abstract class ParadoxComplexExpressionTest : BasePlatformTestCase() {
    protected fun initConfigGroup(gameType: ParadoxGameType) = PlsFacade.getConfigGroupService().let { svc ->
        val groups = svc.getConfigGroups(project).values
        runBlocking { svc.init(groups, project) }
        svc.getConfigGroup(project, gameType)
    }

    protected fun ParadoxComplexExpressionNode.check(dslNode: ParadoxComplexExpressionDslNode) {
        return ParadoxComplexExpressionDslChecker.check(this, dslNode)
    }

    protected fun ParadoxComplexExpressionNode.render(): String {
        return ParadoxComplexExpressionDslRender.render(this)
    }
}

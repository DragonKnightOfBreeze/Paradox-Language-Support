package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxVariableFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxVariableFieldExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxVariableFieldExpression.resolve(text, null, configGroup)
    }

    @Test
    fun testBasic_chain() {
        val s = "root.owner.some_variable"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxVariableFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner", 5..10)
            node<ParadoxOperatorNode>(".", 10..11)
            node<ParadoxDataSourceNode>("some_variable", 11..24)
        }
        exp.check(dsl)
    }

    @Test
    fun testBarrier_noFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxVariableFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDataSourceNode>("owner|x.y", 5..14)
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxVariableFieldExpression>("", 0..0) {
            node<ParadoxDataSourceNode>("", 0..0)
        }
        exp.check(dsl)
    }
}

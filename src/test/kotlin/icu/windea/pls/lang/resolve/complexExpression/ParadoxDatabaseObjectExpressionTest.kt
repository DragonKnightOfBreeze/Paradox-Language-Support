package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDatabaseObjectExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = PlsTestUtil.initConfigGroups(project, ParadoxGameType.Stellaris)

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxDatabaseObjectExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxDatabaseObjectExpression.resolve(text, null, configGroup)
    }

    @Test
    fun testBasic_twoSegments() {
        val s = "civic:some_civic"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>(s, 0..s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0..5)
            node<ParadoxMarkerNode>(":", 5..6)
            node<ParadoxDatabaseObjectNode>("some_civic", 6..16) {
                node<ParadoxDatabaseObjectDataNode>("some_civic", 6..16)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_threeSegments() {
        val s = "civic:some_civic:some_swapped_civic"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>(s, 0..s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0..5)
            node<ParadoxMarkerNode>(":", 5..6)
            node<ParadoxDatabaseObjectNode>("some_civic", 6..16) {
                node<ParadoxDatabaseObjectDataNode>("some_civic", 6..16)
            }
            node<ParadoxMarkerNode>(":", 16..17)
            node<ParadoxDatabaseObjectNode>("some_swapped_civic", 17..35) {
                node<ParadoxDatabaseObjectDataNode>("some_swapped_civic", 17..35)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_job() {
        val s = "job:job_soldier"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>(s, 0..s.length) {
            node<ParadoxDatabaseObjectTypeNode>("job", 0..3)
            node<ParadoxMarkerNode>(":", 3..4)
            node<ParadoxDatabaseObjectNode>("job_soldier", 4..15) {
                node<ParadoxDatabaseObjectDataNode>("job_soldier", 4..15)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>("", 0..0) {
            node<ParadoxDatabaseObjectTypeNode>("", 0..0)
        }
        exp.check(dsl)
    }
}

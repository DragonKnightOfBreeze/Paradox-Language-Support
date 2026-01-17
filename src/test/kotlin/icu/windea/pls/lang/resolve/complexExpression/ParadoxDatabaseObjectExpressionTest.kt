package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
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
class ParadoxDatabaseObjectExpressionTest : ParadoxComplexExpressionTest() {
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
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>(s, 0 to s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0 to 5)
            node<ParadoxMarkerNode>(":", 5 to 6)
            node<ParadoxDatabaseObjectNode>("some_civic", 6 to 16) {
                node<ParadoxDatabaseObjectDataNode>("some_civic", 6 to 16)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_threeSegments() {
        val s = "civic:some_civic:some_swapped_civic"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>(s, 0 to s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0 to 5)
            node<ParadoxMarkerNode>(":", 5 to 6)
            node<ParadoxDatabaseObjectNode>("some_civic", 6 to 16) {
                node<ParadoxDatabaseObjectDataNode>("some_civic", 6 to 16)
            }
            node<ParadoxMarkerNode>(":", 16 to 17)
            node<ParadoxDatabaseObjectNode>("some_swapped_civic", 17 to 35) {
                node<ParadoxDatabaseObjectDataNode>("some_swapped_civic", 17 to 35)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_job() {
        val s = "job:job_soldier"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>(s, 0 to s.length) {
            node<ParadoxDatabaseObjectTypeNode>("job", 0 to 3)
            node<ParadoxMarkerNode>(":", 3 to 4)
            node<ParadoxDatabaseObjectNode>("job_soldier", 4 to 15) {
                node<ParadoxDatabaseObjectDataNode>("job_soldier", 4 to 15)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<ParadoxDatabaseObjectExpression>("", 0 to 0) {
            node<ParadoxDatabaseObjectTypeNode>("", 0 to 0)
        }
        exp.check(dsl)
    }
}

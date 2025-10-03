package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDatabaseObjectExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxDatabaseObjectExpression? {
        val group = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxDatabaseObjectExpression.resolve(text, TextRange(0, text.length), group)
    }

    fun testBasic_twoSegments() {
        val s = "civic:some_civic"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>(s, 0..s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0..5)
            node<ParadoxMarkerNode>(":", 5..6)
            node<ParadoxDatabaseObjectNode>("some_civic", 6..16) {
                node<ParadoxDatabaseObjectDataSourceNode>("some_civic", 6..16)
            }
        }
        exp.check(dsl)
    }

    fun testBasic_threeSegments() {
        val s = "civic:some_civic:some_swapped_civic"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>(s, 0..s.length) {
            node<ParadoxDatabaseObjectTypeNode>("civic", 0..5)
            node<ParadoxMarkerNode>(":", 5..6)
            node<ParadoxDatabaseObjectNode>("some_civic", 6..16) {
                node<ParadoxDatabaseObjectDataSourceNode>("some_civic", 6..16)
            }
            node<ParadoxMarkerNode>(":", 16..17)
            node<ParadoxDatabaseObjectNode>("some_swapped_civic", 17..35) {
                node<ParadoxDatabaseObjectDataSourceNode>("some_swapped_civic", 17..35)
            }
        }
        exp.check(dsl)
    }

    fun testBasic_job() {
        val s = "job:job_soldier"
        val exp = parse(s)!!
        // println(exp.render())
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>(s, 0..s.length) {
            node<ParadoxDatabaseObjectTypeNode>("job", 0..3)
            node<ParadoxMarkerNode>(":", 3..4)
            node<ParadoxDatabaseObjectNode>("job_soldier", 4..15) {
                node<ParadoxDatabaseObjectDataSourceNode>("job_soldier", 4..15)
            }
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        val dsl = buildExpression<ParadoxDatabaseObjectExpression>("", 0..0) {
            node<ParadoxDatabaseObjectTypeNode>("", 0..0)
        }
        exp.check(dsl)
    }
}

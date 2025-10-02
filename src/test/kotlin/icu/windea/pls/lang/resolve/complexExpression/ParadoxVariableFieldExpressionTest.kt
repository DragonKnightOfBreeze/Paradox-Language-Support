package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxVariableFieldExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxVariableFieldExpression? {
        val group = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxVariableFieldExpression.resolve(text, TextRange(0, text.length), group)
    }

    fun testBasic_chain() {
        val s = "root.owner.some_variable"
        val exp = parse(s)!!
        val dsl = buildExpression<ParadoxVariableFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxScopeLinkNode>("owner", 5..10)
            node<ParadoxOperatorNode>(".", 10..11)
            node<ParadoxDataSourceNode>("some_variable", 11..24)
        }
        exp.check(dsl)
    }

    fun testBarrier_noFurtherSplit() {
        val s = "root.owner|x.y"
        val exp = parse(s)!!
        val dsl = buildExpression<ParadoxVariableFieldExpression>(s, 0..s.length) {
            node<ParadoxScopeLinkNode>("root", 0..4)
            node<ParadoxOperatorNode>(".", 4..5)
            node<ParadoxDataSourceNode>("owner|x.y", 5..14)
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        val dsl = buildExpression<ParadoxVariableFieldExpression>("", 0..0) {
            node<ParadoxDataSourceNode>("", 0..0)
        }
        exp.check(dsl)
    }
}

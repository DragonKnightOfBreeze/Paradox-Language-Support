package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefinePrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineReferenceExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxDefineReferenceExpression? {
        PlsTestUtil.initConfigGroup(this.project, gameType)
        val group = Unit
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        return ParadoxDefineReferenceExpression.resolve(text, TextRange(0, text.length), group)
    }

    fun testBasic() {
        val s = "define:NPortrait|GRACEFUL_AGING_START"
        val exp = parse(s)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxDefineReferenceExpression>(s, 0..s.length) {
            node<ParadoxDefinePrefixNode>("define:", 0..7)
            node<ParadoxDefineNamespaceNode>("NPortrait", 7..16)
            node<ParadoxMarkerNode>("|", 16..17)
            node<ParadoxDefineVariableNode>("GRACEFUL_AGING_START", 17..37)
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", incomplete = false))
        val exp = parse("", incomplete = true)!!
        println(exp.render())
        val dsl = buildExpression<ParadoxDefineReferenceExpression>("", 0..0) {
            node<ParadoxErrorTokenNode>("", 0..0)
        }
        exp.check(dsl)
    }
}

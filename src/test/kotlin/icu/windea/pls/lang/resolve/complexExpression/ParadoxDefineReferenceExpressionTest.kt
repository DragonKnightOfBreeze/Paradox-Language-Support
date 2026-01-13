package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefinePrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineReferenceExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    private fun parse(
        text: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false
    ): ParadoxDefineReferenceExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        return ParadoxDefineReferenceExpression.resolve(text, null, configGroup)
    }

    @Test
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

    @Test
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

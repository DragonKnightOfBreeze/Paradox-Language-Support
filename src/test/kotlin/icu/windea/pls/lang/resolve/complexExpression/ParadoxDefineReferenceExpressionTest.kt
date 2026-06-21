package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
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
    fun doSetUp() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxDefineReferenceExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) ChronicleThreadContext.incompleteComplexExpression.set(true) else ChronicleThreadContext.incompleteComplexExpression.remove()
        return ParadoxDefineReferenceExpression.resolve(text, null, configGroup)
    }

    @Test
    fun test_basic() {
        val s = "Namespace|Variable"
        val exp = resolve(s)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>(s, 0, 18) {
            node<ParadoxDefineNamespaceNode>("Namespace", 0, 9)
            node<ParadoxMarkerNode>("|", 9, 10)
            node<ParadoxDefineVariableNode>("Variable", 10, 18)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty_incompleteDiff() {
        Assert.assertNull(resolve("", incomplete = false))
        val exp = resolve("", incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>("", 0, 0) {
            node<ParadoxErrorTokenNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}

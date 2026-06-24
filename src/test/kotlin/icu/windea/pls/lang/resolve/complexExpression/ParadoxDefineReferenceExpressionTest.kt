package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
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

/**
 * @see ParadoxDefineReferenceExpression
 */
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

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxDefineReferenceExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return mark(incomplete) { ParadoxDefineReferenceExpression.resolve(text, null, configGroup) }
    }

    @Test
    fun test_basic() {
        val s = "Namespace|Name"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>("Namespace|Name", 0, 14) {
            node<ParadoxDefineNamespaceNode>("Namespace", 0, 9)
            node<ParadoxMarkerNode>("|", 9, 10)
            node<ParadoxDefineVariableNode>("Name", 10, 14)
        }
        exp.check(dsl)
    }

    @Test
    fun test_missingPipe() {
        val s = "Namespace"
        val exp = resolve(s, ParadoxGameType.Stellaris)
        assertNull(exp)
    }

    @Test
    fun test_missingPipe_incomplete() {
        val s = "Namespace"
        val exp = resolve(s, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>("Namespace", 0, 9) {
            node<ParadoxDefineNamespaceNode>("Namespace", 0, 9)
        }
        exp.check(dsl)
    }

    @Test
    fun test_trailingPipe1() {
        val s = "Namespace|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>("Namespace|", 0, 10) {
            node<ParadoxDefineNamespaceNode>("Namespace", 0, 9)
            node<ParadoxMarkerNode>("|", 9, 10)
            node<ParadoxDefineVariableNode>("", 10, 10)
        }
        exp.check(dsl)
    }

    @Test
    fun test_trailingPipe1_incomplete() {
        val s = "Namespace|"
        val exp = resolve(s, ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>("Namespace|", 0, 10) {
            node<ParadoxDefineNamespaceNode>("Namespace", 0, 9)
            node<ParadoxMarkerNode>("|", 9, 10)
            node<ParadoxDefineVariableNode>("", 10, 10)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxDefineReferenceExpression>("", 0, 0) {
            node<ParadoxDefineNamespaceNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}

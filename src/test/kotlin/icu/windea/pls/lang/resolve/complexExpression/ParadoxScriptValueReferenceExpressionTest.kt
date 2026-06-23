package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptValueReferenceExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxScriptValueReferenceExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return mark(incomplete) { ParadoxScriptValueReferenceExpression.resolve(text, null, configGroup) }
    }

    @Test
    fun test_basic() {
        val s = "some_sv"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxScriptValueReferenceExpression>("some_sv", 0, 7) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_withSimpleArg() {
        val s = "some_sv|PARAM|VALUE|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxScriptValueReferenceExpression>("some_sv|PARAM|VALUE|", 0, 20) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
            node<ParadoxMarkerNode>("|", 7, 8)
            node<ParadoxScriptValueArgumentNameNode>("PARAM", 8, 13)
            node<ParadoxMarkerNode>("|", 13, 14)
            node<ParadoxScriptValueArgumentValueNode>("VALUE", 14, 19)
            node<ParadoxMarkerNode>("|", 19, 20)
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_withMultipleArgs() {
        val s = "some_sv|P1|V1|P2|V2"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxScriptValueReferenceExpression>("some_sv|P1|V1|P2|V2", 0, 19) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
            node<ParadoxMarkerNode>("|", 7, 8)
            node<ParadoxScriptValueArgumentNameNode>("P1", 8, 10)
            node<ParadoxMarkerNode>("|", 10, 11)
            node<ParadoxScriptValueArgumentValueNode>("V1", 11, 13)
            node<ParadoxMarkerNode>("|", 13, 14)
            node<ParadoxScriptValueArgumentNameNode>("P2", 14, 16)
            node<ParadoxMarkerNode>("|", 16, 17)
            node<ParadoxScriptValueArgumentValueNode>("V2", 17, 19)
        }
        exp.check(dsl)
    }

    @Test
    fun test_trailingPipe1_accepted() {
        val s = "some_sv|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxScriptValueReferenceExpression>("some_sv|", 0, 8) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
            node<ParadoxMarkerNode>("|", 7, 8)
        }
        exp.check(dsl)
    }

    @Test
    fun test_trailingPipe2_accepted() {
        val s = "some_sv|P|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxScriptValueReferenceExpression>("some_sv|P|", 0, 10) {
            node<ParadoxScriptValueNode>("some_sv", 0, 7)
            node<ParadoxMarkerNode>("|", 7, 8)
            node<ParadoxScriptValueArgumentNameNode>("P", 8, 9)
            node<ParadoxMarkerNode>("|", 9, 10)
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxScriptValueReferenceExpression>("", 0, 0) {
            node<ParadoxScriptValueNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}

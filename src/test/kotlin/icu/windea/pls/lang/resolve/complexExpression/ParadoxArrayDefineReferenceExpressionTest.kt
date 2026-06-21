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
class ParadoxArrayDefineReferenceExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markConfigDirectory("features/complexExpression/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, gameType: ParadoxGameType, incomplete: Boolean = false): ParadoxArrayDefineReferenceExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) ChronicleThreadContext.incompleteComplexExpression.set(true) else ChronicleThreadContext.incompleteComplexExpression.remove()
        return ParadoxArrayDefineReferenceExpression.resolve(text, null, configGroup)
    }

    @Test
    fun test_basic() {
        val s = "Namespace|Name|0"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpressionStub<ParadoxDefineReferenceExpression>("TODO 2.1.10")
        exp.check(dsl)
    }

    @Test
    fun test_missingPipe() {
        val s = "Namespace"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpressionStub<ParadoxArrayDefineReferenceExpression>("TODO 2.1.10")
        exp.check(dsl)
    }

    @Test
    fun test_trailingPipe1() {
        val s = "Namespace|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpressionStub<ParadoxArrayDefineReferenceExpression>("TODO 2.1.10")
        exp.check(dsl)
    }

    @Test
    fun test_trailingPipe2() {
        val s = "Namespace|Name|"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpressionStub<ParadoxArrayDefineReferenceExpression>("TODO 2.1.10")
        exp.check(dsl)
    }

    @Test
    fun test_notLiteralCompatibleButInvalid() {
        val s = "Namespace|Name|foo123"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpressionStub<ParadoxArrayDefineReferenceExpression>("TODO 2.1.10")
        exp.check(dsl)
    }

    @Test
    fun test_notNumberLiteralCompatibleButInvalid() {
        val s = "Namespace|Name|foo"
        val exp = resolve(s, ParadoxGameType.Stellaris)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpressionStub<ParadoxArrayDefineReferenceExpression>("TODO 2.1.10")
        exp.check(dsl)
    }

    @Test
    fun test_empty() {
        Assert.assertNull(resolve("", ParadoxGameType.Stellaris, incomplete = false))
        val exp = resolve("", ParadoxGameType.Stellaris, incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxArrayDefineReferenceExpression>("", 0, 0) {
            node<ParadoxErrorTokenNode>("", 0, 0)
        }
        exp.check(dsl)
    }
}

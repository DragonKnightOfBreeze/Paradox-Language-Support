package icu.windea.pls.lang.util.evaluators

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxDefineReferenceExpressionEvaluator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefineReferenceExpressionEvaluatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/evaluators")
        markConfigDirectory("chronicle/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/00_defines.txt")
        myFixture.configureByFile("chronicle/common/defines/00_defines.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/entrance.txt")
        myFixture.configureByFile("features/evaluators/define_reference_simple.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val list = toStringList(file)
        val evaluator = ParadoxDefineReferenceExpressionEvaluator()

        assertResult("here_we_introduce") { evaluator.evaluate(list[0]) }
        assertResult(null) { evaluator.evaluate(list[1]) }
    }

    @Test
    fun requireSemantic() {
        myFixture.configureByFile("features/evaluators/define_reference_simple.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val list = toStringList(file)
        val evaluator = ParadoxDefineReferenceExpressionEvaluator()

        assertResult(null) { evaluator.evaluate(list[0]) }
        assertResult(null) { evaluator.evaluate(list[1]) }
    }

    private fun toStringList(file: ParadoxScriptFile): List<ParadoxScriptString> {
        return file.properties().map { it.propertyValue as ParadoxScriptString }.toList()
    }

    private fun assertResult(expect: String?, expression: () -> ParadoxScriptValue?) {
        Assert.assertEquals(expect, expression()?.value)
    }
}

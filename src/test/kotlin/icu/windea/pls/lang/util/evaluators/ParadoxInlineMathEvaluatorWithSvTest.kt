package icu.windea.pls.lang.util.evaluators

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxInlineMathEvaluatorWithSvTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun simpleWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_simple_with_sv.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_simple_with_sv.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(2) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k2"), mapOf("v" to "NaN")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k2"), mapOf("v" to "1")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "NaN")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "2")) }
    }

    @Test
    fun overrideWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_override.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_override.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(2) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k2")) }
    }

    @Test
    fun multiOverrideWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_multi_override.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_multi_override.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(2.5f) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(5) { evaluator.evaluate(map.getValue("k3")) }
    }

    @Test
    fun crossFileNotVisibleWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_cross_file_main.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_cross_file_main.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val otherVirtualFile = myFixture.copyFileToProject(
            "features/evaluators/evaluator_sv_cross_file_other.test.txt",
            "common/evaluator_sv_cross_file_other.test.txt"
        )
        PlsTestUtil.injectFileInfo(otherVirtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_cross_file_other.test.txt")
        FileBasedIndex.getInstance().requestReindex(otherVirtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k1")) }
    }

    @Test
    fun beforeOnlyWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_before_only.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_before_only.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k1")) }
    }

    @Test
    fun inlineMathValueWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_inline_math_basic.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_inline_math_basic.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(8) { evaluator.evaluate(map.getValue("k1")) }
    }

    @Test
    fun inlineMathValueWithParamWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_inline_math_with_param.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_inline_math_with_param.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(8) { evaluator.evaluate(map.getValue("k1"), mapOf("\$NUM$" to "2")) }
    }

    @Test
    fun inlineMathValueMultiLevelWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_inline_math_multi_level.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_inline_math_multi_level.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(4) { evaluator.evaluate(map.getValue("k1")) }
    }

    @Test
    fun inlineMathValueRecursionWithSv() {
        myFixture.configureByFile("features/evaluators/evaluator_sv_inline_math_recursion.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, ParadoxGameType.Stellaris, "common/evaluator_sv_inline_math_recursion.test.txt")
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k1")) }
    }

    private fun toInlineMathMap(file: ParadoxScriptFile): Map<String, ParadoxScriptInlineMath> {
        return file.properties().toList()
            .associateBy({ it.name }, { it.propertyValue })
            .mapValues { (_, v) -> v as ParadoxScriptInlineMath }
    }

    private fun assertResult(expect: Number, expression: () -> MathResult) {
        Assert.assertEquals(expect, expression().normalized())
    }

    private fun assertResult(expect: Class<out Throwable>, expression: () -> MathResult) {
        Assert.assertThrows(expect) { expression().normalized() }
    }
}

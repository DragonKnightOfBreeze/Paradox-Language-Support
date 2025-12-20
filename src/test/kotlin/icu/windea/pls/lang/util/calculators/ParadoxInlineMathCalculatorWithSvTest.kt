package icu.windea.pls.lang.util.calculators

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
class ParadoxInlineMathCalculatorWithSvTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun simpleWithSv() {
        myFixture.configureByFile("features/calculators/calculator_simple_with_sv.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_simple_with_sv.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(2) { calculator.calculate(map.getValue("k1")) }
        assertResult(2) { calculator.calculate(map.getValue("k2")) }
        assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("v" to "NaN")) }
        assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("v" to "1")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k2"), mapOf("var" to "NaN")) }
        assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("var" to "1")) }
        assertResult(3) { calculator.calculate(map.getValue("k2"), mapOf("var" to "2")) }
    }

    @Test
    fun overrideWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_override.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_override.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(2) { calculator.calculate(map.getValue("k1")) }
        assertResult(3) { calculator.calculate(map.getValue("k2")) }
    }

    @Test
    fun multiOverrideWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_multi_override.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_multi_override.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(2.5f) { calculator.calculate(map.getValue("k1")) }
        assertResult(3) { calculator.calculate(map.getValue("k2")) }
        assertResult(5) { calculator.calculate(map.getValue("k3")) }
    }

    @Test
    fun crossFileNotVisibleWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_cross_file_main.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_cross_file_main.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val otherVirtualFile = myFixture.copyFileToProject(
            "features/calculators/calculator_sv_cross_file_other.test.txt",
            "common/calculator_sv_cross_file_other.test.txt"
        )
        PlsTestUtil.injectFileInfo(otherVirtualFile, "common/calculator_sv_cross_file_other.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(otherVirtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k1")) }
    }

    @Test
    fun beforeOnlyWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_before_only.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_before_only.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k1")) }
    }

    @Test
    fun inlineMathValueWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_inline_math_basic.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_inline_math_basic.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(8) { calculator.calculate(map.getValue("k1")) }
    }

    @Test
    fun inlineMathValueWithParamWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_inline_math_with_param.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_inline_math_with_param.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k1")) }
        assertResult(8) { calculator.calculate(map.getValue("k1"), mapOf("\$NUM$" to "2")) }
    }

    @Test
    fun inlineMathValueMultiLevelWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_inline_math_multi_level.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_inline_math_multi_level.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(4) { calculator.calculate(map.getValue("k1")) }
    }

    @Test
    fun inlineMathValueRecursionWithSv() {
        myFixture.configureByFile("features/calculators/calculator_sv_inline_math_recursion.test.txt")
        PlsTestUtil.injectFileInfo(myFixture.file.virtualFile, "common/calculator_sv_inline_math_recursion.test.txt", ParadoxGameType.Stellaris)
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)

        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k1")) }
    }

    private fun toInlineMathMap(file: ParadoxScriptFile): Map<String, ParadoxScriptInlineMath> {
        return file.properties().toList()
            .associateBy({ it.name }, { it.propertyValue })
            .mapValues { (_, v) -> v as ParadoxScriptInlineMath }
    }

    private fun assertResult(expect: Number, expression: () -> CalculatorResult) {
        Assert.assertEquals(expect, expression().resolveValue())
    }

    private fun assertResult(expect: Class<out Throwable>, expression: () -> CalculatorResult) {
        Assert.assertThrows(expect) { expression().resolveValue() }
    }
}

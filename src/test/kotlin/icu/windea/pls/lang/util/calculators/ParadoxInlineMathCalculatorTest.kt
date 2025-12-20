package icu.windea.pls.lang.util.calculators

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxInlineMathCalculatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun simple() {
        myFixture.configureByFile("features/calculators/calculator_simple.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(2) { calculator.calculate(map.getValue("k1")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k2")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k2"), mapOf("v" to "NaN")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k2"), mapOf("v" to "1")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k2"), mapOf("var" to "NaN")) }
        assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("var" to "1")) }
        assertResult(3) { calculator.calculate(map.getValue("k2"), mapOf("var" to "2")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k3")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k3"), mapOf("\$N$" to "NaN")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k3"), mapOf("\$N$" to "1")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k3"), mapOf("\$NUM$" to "NaN")) }
        assertResult(2) { calculator.calculate(map.getValue("k3"), mapOf("\$NUM$" to "1")) }
        assertResult(3) { calculator.calculate(map.getValue("k3"), mapOf("\$NUM$" to "2")) }
        assertResult(2) { calculator.calculate(map.getValue("k4")) }
        assertResult(2) { calculator.calculate(map.getValue("k4"), mapOf("\$N$" to "1")) }
        assertResult(2) { calculator.calculate(map.getValue("k4"), mapOf("\$NUM$" to "1")) }
        assertResult(3) { calculator.calculate(map.getValue("k4"), mapOf("\$NUM$" to "2")) }
    }

    @Test
    fun simpleWithSv() {
        myFixture.configureByFile("features/calculators/calculator_simple_with_sv.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val calculator = ParadoxInlineMathCalculator()

        assertResult(2) { calculator.calculate(map.getValue("k1")) }
        assertResult(2) { calculator.calculate(map.getValue("k2")) }
        // assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("v" to "NaN")) }
        assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("v" to "1")) }
        assertResult(IllegalArgumentException::class.java) { calculator.calculate(map.getValue("k2"), mapOf("var" to "NaN")) }
        assertResult(2) { calculator.calculate(map.getValue("k2"), mapOf("var" to "1")) }
        assertResult(3) { calculator.calculate(map.getValue("k2"), mapOf("var" to "2")) }
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

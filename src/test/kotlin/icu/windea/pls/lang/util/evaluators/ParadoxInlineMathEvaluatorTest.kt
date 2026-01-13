package icu.windea.pls.lang.util.evaluators

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
class ParadoxInlineMathEvaluatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun simple() {
        myFixture.configureByFile("features/evaluators/evaluator_simple.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(2) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("v" to "NaN")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("v" to "1")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "NaN")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "2")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3"), mapOf("\$N$" to "NaN")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3"), mapOf("\$N$" to "1")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "NaN")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "2")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k4"), mapOf("\$N$" to "1")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k4"), mapOf("\$NUM$" to "1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k4"), mapOf("\$NUM$" to "2")) }
    }

    @Test
    fun basicOps() {
        myFixture.configureByFile("features/evaluators/evaluator_basic_ops.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(7) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(9) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(1) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(1) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k5")) }
        assertResult(0.5f) { evaluator.evaluate(map.getValue("k6")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(1) { evaluator.evaluate(map.getValue("k8")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k9")) }
    }

    @Test
    fun complex() {
        myFixture.configureByFile("features/evaluators/evaluator_complex.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(5) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(21) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k5")) }
        assertResult(4) { evaluator.evaluate(map.getValue("k6")) }
        assertResult(2.5f) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k8")) }
        assertResult(7) { evaluator.evaluate(map.getValue("k9")) }
    }

    @Test
    fun paramsEdge() {
        myFixture.configureByFile("features/evaluators/evaluator_params_edge.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(2) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "1")) }
        assertResult(30) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM|1$" to "10", "\$NUM|2$" to "20")) }

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k4")) }

        assertResult(3) { evaluator.evaluate(map.getValue("k5"), mapOf("\$NUM|1$" to "2")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k6"), mapOf("\$NUM$" to "2")) }

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k7"), mapOf("\$MISS$" to "NaN")) }
    }

    @Test
    fun errors() {
        myFixture.configureByFile("features/evaluators/evaluator_errors.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k5")) }
    }

    @Test
    fun edgeCases() {
        myFixture.configureByFile("features/evaluators/evaluator_edge_cases.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(9) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(9) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(2) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(8) { evaluator.evaluate(map.getValue("k4")) }

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k5")) }
        assertResult(3) { evaluator.evaluate(map.getValue("k5"), mapOf("\$NUM$" to "2")) }

        assertResult(2) { evaluator.evaluate(map.getValue("k6"), mapOf("\$NUM$" to "3")) }
        assertResult(1.5f) { evaluator.evaluate(map.getValue("k6"), mapOf("\$NUM$" to "2")) }

        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k8")) }
        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k9")) }
        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k10")) }
        assertResult(IllegalStateException::class.java) { evaluator.evaluate(map.getValue("k11")) }
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

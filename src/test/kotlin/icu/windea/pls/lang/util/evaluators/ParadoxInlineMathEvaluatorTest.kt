package icu.windea.pls.lang.util.evaluators

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxInlineMathEvaluatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun simple() {
        myFixture.configureByFile("features/evaluators/evaluator_simple.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("v" to "NaN")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("v" to "1")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "NaN")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "1")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k2"), mapOf("var" to "2")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3"), mapOf("\$N$" to "NaN")) }
        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k3"), mapOf("\$N$" to "1")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "NaN")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "1")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "2")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k4"), mapOf("\$N$" to "1")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k4"), mapOf("\$NUM$" to "1")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k4"), mapOf("\$NUM$" to "2")) }
    }

    @Test
    fun basicOps() {
        myFixture.configureByFile("features/evaluators/evaluator_basic_ops.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(MathResult.from(7)) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(MathResult.from(9)) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(MathResult.from(1)) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(MathResult.from(1)) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k5")) }
        assertResult(MathResult.from(0.5)) { evaluator.evaluate(map.getValue("k6")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(MathResult.from(1)) { evaluator.evaluate(map.getValue("k8")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k9")) }
    }

    @Test
    fun complex() {
        myFixture.configureByFile("features/evaluators/evaluator_complex.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(MathResult.from(5)) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(MathResult.from(21)) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k4")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k5")) }
        assertResult(MathResult.from(4)) { evaluator.evaluate(map.getValue("k6")) }
        assertResult(MathResult.from(2.5)) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k8")) }
        assertResult(MathResult.from(7)) { evaluator.evaluate(map.getValue("k9")) }
    }

    @Test
    fun paramsEdge() {
        myFixture.configureByFile("features/evaluators/evaluator_params_edge.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val map = toInlineMathMap(file)
        val evaluator = ParadoxInlineMathEvaluator()

        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM$" to "1")) }
        assertResult(MathResult.from(30)) { evaluator.evaluate(map.getValue("k3"), mapOf("\$NUM|1$" to "10", "\$NUM|2$" to "20")) }

        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k4")) }

        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k5"), mapOf("\$NUM|1$" to "2")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k6"), mapOf("\$NUM$" to "2")) }

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k7")) }
        assertResult(ArithmeticException::class.java) { evaluator.evaluate(map.getValue("k7"), mapOf("\$MISS$" to "NaN")) }
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

        assertResult(MathResult.from(9)) { evaluator.evaluate(map.getValue("k1")) }
        assertResult(MathResult.from(9)) { evaluator.evaluate(map.getValue("k2")) }
        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k3")) }
        assertResult(MathResult.from(8)) { evaluator.evaluate(map.getValue("k4")) }

        assertResult(IllegalArgumentException::class.java) { evaluator.evaluate(map.getValue("k5")) }
        assertResult(MathResult.from(3)) { evaluator.evaluate(map.getValue("k5"), mapOf("\$NUM$" to "2")) }

        assertResult(MathResult.from(2)) { evaluator.evaluate(map.getValue("k6"), mapOf("\$NUM$" to "3")) }
        assertResult(MathResult.from(1.5)) { evaluator.evaluate(map.getValue("k6"), mapOf("\$NUM$" to "2")) }

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

    private fun assertResult(expect: MathResult, expression: () -> MathResult) {
        Assert.assertEquals(expect, expression())
    }

    private fun assertResult(expect: Class<out Throwable>, expression: () -> MathResult) {
        Assert.assertThrows(expect) { expression() }
    }
}

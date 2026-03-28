package icu.windea.pls.lang.psi

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.select.asProperty
import icu.windea.pls.lang.select.ofPath
import icu.windea.pls.lang.select.one
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.script.psi.ParadoxScriptFile
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
class ParadoxPsiManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun getArgumentTupleList() {
        val file = myFixture.configureByFile("script/stubs/argument_aware_elements.test.txt") as ParadoxScriptFile

        run {
            val expected = listOf("PARAM_1" to "foo", "PARAM_2" to "123", "PARAM_3" to "123.456")
            val property = selectScope { file.ofPath("some_scripted_trigger").asProperty().one() }!!
            val args = ParadoxPsiManager.getArgumentTupleList(property.block!!)
            Assert.assertEquals(expected, args)
        }

        run {
            val expected = listOf("VAR" to "@var", "PARAM" to "\$PARAM$", "NUM" to "@[ 1 + 1 ]")
            val property = selectScope { file.ofPath("some_scripted_effect").asProperty().one() }!!
            val args = ParadoxPsiManager.getArgumentTupleList(property.block!!)
            Assert.assertEquals(expected, args)
        }

        run {
            // Keep quotes of argument values
            val expected = listOf("P1" to "\$PARAM$", "P2" to "\"\$OTHER_PARAM$\"", "P3" to "bar", "P4" to "yes")
            val property = selectScope { file.ofPath("inline_script").asProperty().elementAt(0) }
            val args = ParadoxPsiManager.getArgumentTupleList(property.block!!, "script")
            Assert.assertEquals(expected, args)
        }

        run {
            // Accept only valid identifier characters (leading numbers are allowed)
            val expected = listOf("VALID_IDENTIFIER" to "v", "00_INVALID_IDENTIFIER" to "v")
            val property = selectScope { file.ofPath("inline_script").asProperty().elementAt(1) }
            val args = ParadoxPsiManager.getArgumentTupleList(property.block!!, "script")
            Assert.assertEquals(expected, args)
        }
    }

}

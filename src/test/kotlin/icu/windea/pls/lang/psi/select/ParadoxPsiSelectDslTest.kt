package icu.windea.pls.lang.psi.select

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxPsiSelectDslTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun ofKey() {
        val file = configureScriptFile("script/syntax/code_settings.test.txt")
        val settings = selectScope { file.properties().ofKey("settings").one() }
        Assert.assertNotNull(settings)

        val stringProps = selectScope { settings!!.properties().ofKey("string_value").all() }
        Assert.assertEquals(2, stringProps.size)
    }

    @Test
    fun ofValue_and_ofValues_normalization() {
        val file = configureScriptFile("script/syntax/code_settings.test.txt")
        val settings = selectScope { file.properties().ofKey("settings").one() }!!

        val fooProps = selectScope { settings.properties().ofValue("Foo").all() }
        Assert.assertEquals(1, fooProps.size)

        val escapedNewLineProps = selectScope { settings.properties().ofValue("Foo\\n bar ").all() }
        Assert.assertEquals(1, escapedNewLineProps.size)

        val list = selectScope { settings.properties().ofValues(listOf("yes", "1.0")).all() }
        Assert.assertTrue(list.size >= 2)
    }

    @Test
    fun ofPath_basic() {
        val file = configureScriptFile("script/syntax/code_settings.test.txt")

        val settings = selectScope { file.properties().ofKey("settings").one() }!!
        val stringProp = selectScope { settings.ofPath("string_value").one() }
        Assert.assertNotNull(stringProp)
        Assert.assertEquals("string_value", stringProp!!.name)

        val stringProps = selectScope { file.ofPath("settings/string_value").asProperty().all() }
        Assert.assertEquals(2, stringProps.size)
    }

    @Test
    fun walkUp_and_parentOfKey() {
        val file = configureScriptFile("script/syntax/code_settings.test.txt")
        val value = selectScope { file.ofPath("settings/string_value").asProperty().one()!!.propertyValue }!!

        val propertyFromWalkUp = selectScope { value.walkUp().asProperty().one() }
        Assert.assertNotNull(propertyFromWalkUp)
        Assert.assertEquals("string_value", propertyFromWalkUp!!.name)

        val settings = selectScope { value.parentOfKey("settings") }
        Assert.assertNotNull(settings)
        Assert.assertEquals("settings", settings!!.name)
    }

    private fun configureScriptFile(path: String): ParadoxScriptFile {
        myFixture.configureByFile(path)
        return myFixture.file as ParadoxScriptFile
    }
}

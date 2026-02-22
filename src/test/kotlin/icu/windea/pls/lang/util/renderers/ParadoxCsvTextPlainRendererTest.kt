package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.rd.util.AtomicInteger
import icu.windea.pls.csv.psi.ParadoxCsvFile
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
class ParadoxCsvTextPlainRendererTest : BasePlatformTestCase() {
    private val counter = AtomicInteger()

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun commentOnly_shouldRenderEmpty() {
        assertResult(
            "",
            "# only comments\n# nothing to parse\n"
        )
    }

    @Test
    fun commentsAndWhitespaces_shouldBeRemoved() {
        assertResult(
            "name;age\nalice;18",
            "# comment\n name ; age \n\n # comment2\n alice ; 18 \n"
        )
    }

    @Test
    fun emptyColumnsAndTrailingSeparator_shouldKeepSeparators() {
        assertResult(
            "name;age;desc;\n;18;",
            "name;age;desc;\n;18;\n"
        )
    }

    @Test
    fun quoteOnlyIfNecessary_semicolon() {
        assertResult(
            "a;b\n\"x;y\";z",
            "a ; b\n  \"x;y\" ;  z\n"
        )
    }

    @Test
    fun quoteOnlyIfNecessary_quoteAndEscape() {
        assertResult(
            "a\n\"a\\\"b\"",
            "a\n\"a\\\"b\"\n"
        )
    }

    @Test
    fun quoteOnlyIfNecessary_boundaryWhitespaces() {
        assertResult(
            "a\n\"  b\"",
            "a\n\"  b\"\n"
        )
        assertResult(
            "a\n\"b  \"",
            "a\n\"b  \"\n"
        )
    }

    private fun assertResult(expect: String, input: String) {
        val id = counter.getAndIncrement()
        myFixture.configureByText("renderer_test_$id.csv", input)
        val file = myFixture.file as ParadoxCsvFile
        val renderer = ParadoxCsvTextPlainRenderer()
        val result = renderer.render(file)
        Assert.assertEquals(expect, result)
    }
}

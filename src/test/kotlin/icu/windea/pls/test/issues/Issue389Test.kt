package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.references.ParadoxComplexEnumValuePsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertIs

/**
 * See: [#389](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/389)
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue389Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/389")
        markConfigDirectory("issues/389/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testReferenceResolution_Number_WriteAccess() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            test = {
                123 abc
                enum = {
                    <caret>123 abc
                }
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxComplexEnumValuePsiReference>(reference)
            val resolved = reference.resolve()
            assertIs<ParadoxComplexEnumValueLightElement>(resolved)
            assertEquals("123", resolved.name)
            assertEquals("test_enum", resolved.enumName)
            assertEquals(ReadWriteAccess.Write, resolved.readWriteAccess)
        }
    }

    @Test
    fun testReferenceResolution_String_WriteAccess() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            test = {
                123 abc
                enum = {
                    123 <caret>abc
                }
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxComplexEnumValuePsiReference>(reference)
            val resolved = reference.resolve()
            assertIs<ParadoxComplexEnumValueLightElement>(resolved)
            assertEquals("abc", resolved.name)
            assertEquals("test_enum", resolved.enumName)
            assertEquals(ReadWriteAccess.Write, resolved.readWriteAccess)
        }
    }

    @Test
    fun testReferenceResolution_Number_ReadAccess() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            test = {
                <caret>123 abc
                enum = {
                    123 abc
                }
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxScriptExpressionPsiReference>(reference)
            val resolved = reference.resolve()
            assertIs<ParadoxComplexEnumValueLightElement>(resolved)
            assertEquals("123", resolved.name)
            assertEquals("test_enum", resolved.enumName)
            assertEquals(ReadWriteAccess.Read, resolved.readWriteAccess)
        }
    }

    @Test
    fun testReferenceResolution_String_ReadAccess() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            test = {
                123 <caret>abc
                enum = {
                    123 abc
                }
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxScriptExpressionPsiReference>(reference)
            val resolved = reference.resolve()
            assertIs<ParadoxComplexEnumValueLightElement>(resolved)
            assertEquals("abc", resolved.name)
            assertEquals("test_enum", resolved.enumName)
            assertEquals(ReadWriteAccess.Read, resolved.readWriteAccess)
        }
    }
}

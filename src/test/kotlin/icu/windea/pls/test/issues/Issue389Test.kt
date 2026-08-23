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
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

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
        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            reference.expectIs<ParadoxComplexEnumValuePsiReference>()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxComplexEnumValueLightElement>()
            resolved.name.expectEquals("123")
            resolved.enumName.expectEquals("test_enum")
            resolved.readWriteAccess.expectEquals(ReadWriteAccess.Write)
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
        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            reference.expectIs<ParadoxComplexEnumValuePsiReference>()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxComplexEnumValueLightElement>()
            resolved.name.expectEquals("abc")
            resolved.enumName.expectEquals("test_enum")
            resolved.readWriteAccess.expectEquals(ReadWriteAccess.Write)
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
        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            reference.expectIs<ParadoxScriptExpressionPsiReference>()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxComplexEnumValueLightElement>()
            resolved.name.expectEquals("123")
            resolved.enumName.expectEquals("test_enum")
            resolved.readWriteAccess.expectEquals(ReadWriteAccess.Read)
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
        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            reference.expectIs<ParadoxScriptExpressionPsiReference>()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxComplexEnumValueLightElement>()
            resolved.name.expectEquals("abc")
            resolved.enumName.expectEquals("test_enum")
            resolved.readWriteAccess.expectEquals(ReadWriteAccess.Read)
        }
    }
}

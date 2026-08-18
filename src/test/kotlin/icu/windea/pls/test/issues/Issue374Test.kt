package icu.windea.pls.test.issues

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#374](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/374)
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue374Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/374")
        markConfigDirectory("issues/374/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testInspection() {
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = 1
                value = some_flag
                value = v1
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.checkHighlighting()
    }

    @Test
    fun testReferenceResolution_Enum() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = <caret>v1
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<CwtString>()
            resolved.name.expectEquals("v1")
        }

        myFixture.checkHighlighting()
    }

    @Test
    fun testReferenceResolution_DynamicValue() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = <caret>some_flag
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxDynamicValueLightElement>()
            resolved.name.expectEquals("some_flag")
            resolved.presentableType.expectEquals("test_flag")
        }

        myFixture.checkHighlighting()
    }

    @Test
    fun testCompletion_Enum() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
            }

            test_entity = {
                id = some_other_id
                name = some_name
                value = <caret>
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "v1", "v2")
    }

    @Test
    fun testCompletion_DynamicValue() {
        // NOTE 3.0.1 如果这里不另外加上一行 `value = some_other_flag`，以至于只有唯一一个候选项，调用 `myFixture.complete` 后会直接插入这个候选项，并且修改 PSI。
        //  这会导致意外报错：PSI and index do not match.

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = some_flag
                value = some_other_flag
            }

            test_entity = {
                id = some_other_id
                name = some_name
                value = some_<caret>
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "some_flag", "some_other_flag")
    }
}

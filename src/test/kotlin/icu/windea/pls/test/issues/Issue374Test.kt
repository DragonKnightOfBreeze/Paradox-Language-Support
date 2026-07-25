package icu.windea.pls.test.issues

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#374](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/374)
 *
 * @see UnresolvedExpressionInspection
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

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
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

        val reference = myFixture.findReferenceAtCaret()!!
        val resolved = reference.resolve()!!
        assertTrue(resolved is CwtValue && resolved.name == "v1")

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
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

        val reference = myFixture.findReferenceAtCaret()!!
        val resolved = reference.resolve()!!
        assertTrue(resolved is ParadoxDynamicValueLightElement && resolved.name == "some_flag" && resolved.presentableType == "test_flag")

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun testCompletion_Enum() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = <caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings: List<String> = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "v1", "v2")
    }

    @Test
    fun testCompletion_DynamicValue() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/00_entities.txt")
        myFixture.configureByText("00_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = some_flag
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                id = some_id
                name = some_name
                value = some_<caret>
            }
        """.trimIndent())

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings: List<String> = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "some_flag")
    }
}

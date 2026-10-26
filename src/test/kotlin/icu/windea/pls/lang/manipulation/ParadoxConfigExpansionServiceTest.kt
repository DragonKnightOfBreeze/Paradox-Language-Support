package icu.windea.pls.lang.manipulation

import com.intellij.psi.PsiElement
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxConfigExpansionService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxConfigExpansionServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/config/manipulation")
        markConfigDirectory("features/config/manipulation/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configGroup(): CwtConfigGroup {
        return ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
    }

    private fun configureScript(value: String): PsiElement {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/expansion.test.txt")
        myFixture.configureByText("expansion.test.txt", """
            test_entity = {
                value = $value
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        return myFixture.file
    }

    // region union

    @Test
    fun testExpandAndMatchUnion_exactConstant() {
        val element = configureScript("v1")
        val expression = ParadoxExpression.resolve("v1", false)

        val result = ParadoxConfigExpansionService.expandAndMatchUnion(
            element, expression, "test_union_constant", configGroup(), guardName = "test.expandAndMatchUnion"
        ).toList()
        assertEquals(1, result.size)
        val (expanded, matchResult) = result.single()
        assertEquals("v1", expanded.configExpression.expressionString)
        assertSame(ParadoxMatchResult.ExactMatch, matchResult)
    }

    @Test
    fun testExpandAndMatchUnion_noMatch() {
        val element = configureScript("v3")
        val expression = ParadoxExpression.resolve("v3", false)

        val result = ParadoxConfigExpansionService.expandAndMatchUnion(
            element, expression, "test_union_constant", configGroup(), guardName = "test.expandAndMatchUnion"
        ).toList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testExpandAndMatchUnion_nonConstant() {
        val element = configureScript("42")
        val expression = ParadoxExpression.resolve("42", false)

        val result = ParadoxConfigExpansionService.expandAndMatchUnion(
            element, expression, "test_union", configGroup(), guardName = "test.expandAndMatchUnion"
        ).toList()
        val intResult = result.firstOrNull { it.first.configExpression.expressionString == "int" }
        assertNotNull(intResult)
        assertSame(ParadoxMatchResult.ExactMatch, intResult!!.second)
    }

    @Test
    fun testExpandAndMatchUnion_missingName() {
        val element = configureScript("v1")
        val expression = ParadoxExpression.resolve("v1", false)

        val result = ParadoxConfigExpansionService.expandAndMatchUnion(
            element, expression, "missing_union", configGroup(), guardName = "test.expandAndMatchUnion"
        ).toList()
        assertTrue(result.isEmpty())
    }

    // endregion

    // region alias

    @Test
    fun testExpandAndMatchAlias_exactKey() {
        val element = configureScript("foo")
        val expression = ParadoxExpression.resolve("foo", false)

        val result = ParadoxConfigExpansionService.expandAndMatchAlias(
            element, expression, "test_alias", configGroup(), guardName = "test.expandAndMatchAlias"
        ).toList()
        assertEquals(1, result.size)
        val (expanded, matchResult) = result.single()
        assertEquals("foo", expanded.configExpression.expressionString)
        assertEquals("test_alias", expanded.config.name)
        assertEquals("foo", expanded.config.subName)
        assertEquals(2, expanded.configs.size)
        assertSame(ParadoxMatchResult.ExactMatch, matchResult)
    }

    @Test
    fun testExpandAndMatchAlias_nonConstantKey() {
        val element = configureScript("some_value")
        val expression = ParadoxExpression.resolve("some_value", false)

        val result = ParadoxConfigExpansionService.expandAndMatchAlias(
            element, expression, "test_alias", configGroup(), guardName = "test.expandAndMatchAlias"
        ).toList()
        val scalarResult = result.firstOrNull { it.first.configExpression.expressionString == "scalar" }
        assertNotNull(scalarResult)
        assertEquals(1, scalarResult!!.first.configs.size)
        assertSame(ParadoxMatchResult.FallbackMatch, scalarResult.second)
    }

    @Test
    fun testExpandAndMatchAlias_missingName() {
        val element = configureScript("foo")
        val expression = ParadoxExpression.resolve("foo", false)

        val result = ParadoxConfigExpansionService.expandAndMatchAlias(
            element, expression, "missing_alias", configGroup(), guardName = "test.expandAndMatchAlias"
        ).toList()
        assertTrue(result.isEmpty())
    }

    // endregion
}

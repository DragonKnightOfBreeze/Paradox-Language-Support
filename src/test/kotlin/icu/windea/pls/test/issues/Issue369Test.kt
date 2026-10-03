package icu.windea.pls.test.issues

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.quote
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.text.ParadoxScript
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#369](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/369)
 *
 * Definitions whose names contain spaces (e.g., `name = "spaced out"`) were neither resolved nor completed
 * correctly: completion offered the raw name without quotes, and the quoted form did not resolve either
 * because the quotes were stripped from the definition name. Definitions whose root key acts as the name
 * (e.g., `"spaced out" = {...}`) were also matched as an arbitrary property.
 *
 * Cause: definition names were assumed to be valid identifiers, but they should be allowed to be any string
 * literal. In addition, the completion lookup string was not quoted when the value is blank or contains a
 * blank, so a name containing spaces could not be inserted/resolved correctly.
 *
 * @see icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
 * @see icu.windea.pls.lang.resolve.ParadoxMemberService.getTypeKey
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue369Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/369")
        markConfigDirectory("issues/369/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testInspection() {
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)

        markFileInfo(ParadoxGameType.Stellaris, "prescripted_countries/test_countries.txt")
        myFixture.configureByFile("issues/369/prescripted_countries/test_countries.txt")

        markFileInfo(ParadoxGameType.Stellaris, "map/setup_scenarios/test_setup_scenarios.txt")
        myFixture.configureByFile("issues/369/map/setup_scenarios/test_setup_scenarios.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                setup_scenario = no_spaces
                setup_scenario = "no_spaces"
                setup_scenario = "spaced out"

                country = no_spaces
                country = "no_spaces"
                country = "spaced out"
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testCompletion() {
        markFileInfo(ParadoxGameType.Stellaris, "prescripted_countries/test_countries.txt")
        myFixture.configureByFile("issues/369/prescripted_countries/test_countries.txt")

        markFileInfo(ParadoxGameType.Stellaris, "map/setup_scenarios/test_setup_scenarios.txt")
        myFixture.configureByFile("issues/369/map/setup_scenarios/test_setup_scenarios.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                setup_scenario = <caret>
                # ...
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out".quote(QuotePatterns.ParadoxScript)) // should be quoted if is blank or contains blank
    }

    @Test
    fun testCompletion_Another() {
        markFileInfo(ParadoxGameType.Stellaris, "prescripted_countries/test_countries.txt")
        myFixture.configureByFile("issues/369/prescripted_countries/test_countries.txt")

        markFileInfo(ParadoxGameType.Stellaris, "map/setup_scenarios/test_setup_scenarios.txt")
        myFixture.configureByFile("issues/369/map/setup_scenarios/test_setup_scenarios.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                "country" = <caret>
                # ...
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out".quote(QuotePatterns.ParadoxScript)) // should be quoted if is blank or contains blank
    }

    @Test
    fun testCompletion_AlreadyQuoted() {
        markFileInfo(ParadoxGameType.Stellaris, "prescripted_countries/test_countries.txt")
        myFixture.configureByFile("issues/369/prescripted_countries/test_countries.txt")

        markFileInfo(ParadoxGameType.Stellaris, "map/setup_scenarios/test_setup_scenarios.txt")
        myFixture.configureByFile("issues/369/map/setup_scenarios/test_setup_scenarios.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                setup_scenario = "<caret>"
                # ...
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out") // should not be quoted since already quoted
    }

    @Test
    fun testCompletion_AlreadyLeftQuoted() {
        markFileInfo(ParadoxGameType.Stellaris, "prescripted_countries/test_countries.txt")
        myFixture.configureByFile("issues/369/prescripted_countries/test_countries.txt")

        markFileInfo(ParadoxGameType.Stellaris, "map/setup_scenarios/test_setup_scenarios.txt")
        myFixture.configureByFile("issues/369/map/setup_scenarios/test_setup_scenarios.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_entities/test_entities.txt")
        myFixture.configureByText("test_entities.txt", """
            test_entity = {
                setup_scenario = "<caret>
                # ...
            }
        """.trimIndent())

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out") // should not be quoted since already quoted (even only left quoted)
    }
}

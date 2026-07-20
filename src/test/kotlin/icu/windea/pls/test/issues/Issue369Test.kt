package icu.windea.pls.test.issues

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.quote
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#369](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/369)
 *
 * @see UnresolvedExpressionInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue369Test : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/369")
        markConfigDirectory("issues/369/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun test() {
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

        myFixture.configureFromExistingVirtualFile(myFixture.file.virtualFile) // necessary
        myFixture.checkHighlighting()
    }

    @Test
    fun testCompletion_1() {
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

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings: List<String> = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out".quote()) // should be quoted if is blank or contains blank
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

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings: List<String> = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out".quote()) // should be quoted if is blank or contains blank
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

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings: List<String> = myFixture.lookupElementStrings!!
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

        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings: List<String> = myFixture.lookupElementStrings!!
        assertSameElements(lookupElementStrings, "no_spaces", "spaced out") // should not be quoted since already quoted (even only left quoted)
    }
}

package icu.windea.pls.lang.overrides

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.settings.ParadoxGameSettingsState
import icu.windea.pls.base.settings.ParadoxModDependencySettingsState
import icu.windea.pls.base.settings.ParadoxModSettingsState
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.model.overrides.ParadoxOverrideResult
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxOverrideService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxOverrideServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/overrides")
        // force-refresh the injected config directory, so that test workers with a stale VFS
        // (e.g. reused across runs) always see the current test data
        val injectConfigPath = "src/test/testData".toPathOrNull()?.resolve("features/overrides/.config")
        val injectConfigDir = injectConfigPath?.toVirtualFile(refreshIfNeed = true)
        if (injectConfigDir != null) {
            VfsUtil.markDirtyAndRefresh(false, true, true, injectConfigDir)
            injectConfigDir.children.forEach { VfsUtil.markDirtyAndRefresh(false, true, true, it) }
        }
        markConfigDirectory("features/overrides/.config")
        initInjectedConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getOrderInContext Tests

    @Test
    fun getOrderInContext_gameDirectory_returnsZero() {
        val settings = ParadoxGameSettingsState().apply { gameDirectory = "/game" }
        Assert.assertEquals(0, ParadoxOverrideService.getOrderInContext("/game", settings))
    }

    @Test
    fun getOrderInContext_modDependency_returnsIndexPlusOne() {
        val settings = ParadoxGameSettingsState().apply {
            gameDirectory = "/game"
            modDependencies = mutableListOf(
                ParadoxModDependencySettingsState().apply { modDirectory = "/modA" },
                ParadoxModDependencySettingsState().apply { modDirectory = "/modB" },
            )
        }
        Assert.assertEquals(1, ParadoxOverrideService.getOrderInContext("/modA", settings))
        Assert.assertEquals(2, ParadoxOverrideService.getOrderInContext("/modB", settings))
    }

    @Test
    fun getOrderInContext_selfModDirectory_returnsMaxValue() {
        val settings = ParadoxModSettingsState().apply {
            gameDirectory = "/game"
            modDirectory = "/modSelf"
            modDependencies = mutableListOf(ParadoxModDependencySettingsState().apply { modDirectory = "/dep" })
        }
        Assert.assertEquals(0, ParadoxOverrideService.getOrderInContext("/game", settings))
        Assert.assertEquals(1, ParadoxOverrideService.getOrderInContext("/dep", settings))
        Assert.assertEquals(Int.MAX_VALUE, ParadoxOverrideService.getOrderInContext("/modSelf", settings))
    }

    @Test
    fun getOrderInContext_unknownPath_returnsMinusOne() {
        val settings = ParadoxGameSettingsState().apply { gameDirectory = "/game" }
        Assert.assertEquals(-1, ParadoxOverrideService.getOrderInContext("/unknown", settings))
    }

    // endregion

    // region getOverrideStrategy(target) Tests

    @Test
    fun getOverrideStrategy_configFromCore_returnsConfiguredStrategy() {
        val property = configureProperty("common/test_entities_core/01_test.txt", "01_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, ParadoxOverrideService.getOverrideStrategy(property))
    }

    @Test
    fun getOverrideStrategy_configFromGame_returnsGameStrategy() {
        val property = configureProperty("common/test_entities_game/01_test.txt", "01_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        Assert.assertEquals(ParadoxOverrideStrategy.DUPL, ParadoxOverrideService.getOverrideStrategy(property))
    }

    @Test
    fun getOverrideStrategy_configOrdered_returnsOrdered() {
        val property = configureProperty("common/test_entities_ordered/01_test.txt", "01_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        Assert.assertEquals(ParadoxOverrideStrategy.ORDERED, ParadoxOverrideService.getOverrideStrategy(property))
    }

    @Test
    fun getOverrideStrategy_configAbsent_returnsLios() {
        val property = configureProperty("common/test_entities_default/01_test.txt", "01_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        Assert.assertEquals(ParadoxOverrideStrategy.LIOS, ParadoxOverrideService.getOverrideStrategy(property))
    }

    @Test
    fun getOverrideStrategy_file_returnsForcedFios() {
        markFileInfo(gameType, "common/test_entities_core/01_test.txt")
        myFixture.configureByText("01_test.txt", "my_entity = { a = 1 }")
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, ParadoxOverrideService.getOverrideStrategy(myFixture.file))
    }

    @Test
    fun getOverrideStrategy_onAction_returnsForcedOrdered() {
        val property = configureProperty("common/on_actions/01_on_actions.txt", "01_on_actions.txt", """
            <caret>my_action = { events = { } }
        """)
        Assert.assertEquals(ParadoxOverrideStrategy.ORDERED, ParadoxOverrideService.getOverrideStrategy(property))
    }

    // endregion

    // region getOverrideStrategy(searchParameters) Tests

    @Test
    fun getOverrideStrategy_searchParametersDefinition_returnsConfiguredStrategy() {
        markFileInfo(gameType, "common/test_entities_core/01_test.txt")
        myFixture.configureByText("01_test.txt", "my_entity = { a = 1 }")
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file)
        val parameters = ParadoxDefinitionSearch.Parameters("my_entity", "test_entity_core", selector)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, ParadoxOverrideService.getOverrideStrategy(parameters))
    }

    @Test
    fun getOverrideStrategy_searchParametersScriptedVariable_returnsConfiguredStrategy() {
        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.txt", "@var = 1")
        val selector = ParadoxScriptedVariableSearch.selector(project, myFixture.file)
        val parameters = ParadoxScriptedVariableSearch.Parameters("var", ParadoxScriptedVariableType.Global, selector)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, ParadoxOverrideService.getOverrideStrategy(parameters))
    }

    @Test
    fun getOverrideStrategy_searchParametersFilePath_returnsForcedFios() {
        markFileInfo(gameType, "common/test_entities_core/01_test.txt")
        myFixture.configureByText("01_test.txt", "my_entity = { a = 1 }")
        val selector = ParadoxFilePathSearch.selector(project, myFixture.file)
        val parameters = ParadoxFilePathSearch.Parameters("common/test_entities_core/01_test.txt", null, selector)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, ParadoxOverrideService.getOverrideStrategy(parameters))
    }

    @Test
    fun getOverrideStrategy_searchParametersDefineVariable_returnsLios() {
        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """)
        val selector = ParadoxDefineVariableSearch.selector(project, myFixture.file)
        // define variables are configured by the `common/defines` entry; absent -> default `lios`
        val parameters = ParadoxDefineVariableSearch.Parameters("Namespace", "Variable", selector)
        Assert.assertEquals(ParadoxOverrideStrategy.LIOS, ParadoxOverrideService.getOverrideStrategy(parameters))
    }

    // endregion

    // region getOverrideResultForDefinition Tests

    @Test
    fun getOverrideResultForDefinition_duplicateSamePath_returnsResult() {
        markFileInfo(gameType, "common/test_entities_core/01_test.txt")
        myFixture.configureByText("01_test.txt", "my_entity = { a = 1 }")

        val property = configureProperty("common/test_entities_core/01_test.txt", "01_test.copy.txt", """
            <caret>my_entity = { a = 1 }
        """)
        val result = ParadoxOverrideService.getOverrideResultForDefinition(property, myFixture.file)
        Assert.assertNotNull(result)
        Assert.assertEquals("my_entity", result!!.key)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, result.overrideStrategy)
        Assert.assertTrue(ParadoxOverrideService.isOverrideCorrect(result))
    }

    @Test
    fun getOverrideResultForDefinition_singleOccurrence_returnsNull() {
        val property = configureProperty("common/test_entities_core/01_test.txt", "01_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        Assert.assertNull(ParadoxOverrideService.getOverrideResultForDefinition(property, myFixture.file))
    }

    @Test
    fun getOverrideResultForDefinition_orderedStrategy_returnsNull() {
        markFileInfo(gameType, "common/test_entities_ordered/01_test.txt")
        myFixture.configureByText("01_test.txt", "my_entity = { a = 1 }")

        val property = configureProperty("common/test_entities_ordered/99_test.txt", "99_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        Assert.assertNull(ParadoxOverrideService.getOverrideResultForDefinition(property, myFixture.file))
    }

    @Test
    fun getOverrideResultForDefinition_differentPath_isIncorrect() {
        markFileInfo(gameType, "common/test_entities_core/01_test.txt")
        myFixture.configureByText("01_test.txt", "my_entity = { a = 1 }")

        val property = configureProperty("common/test_entities_core/99_test.txt", "99_test.txt", """
            <caret>my_entity = { a = 1 }
        """)
        val result = ParadoxOverrideService.getOverrideResultForDefinition(property, myFixture.file)
        Assert.assertNotNull(result)
        Assert.assertFalse(ParadoxOverrideService.isOverrideCorrect(result!!))
    }

    // endregion

    // region getOverrideResultForGlobalScriptedVariable Tests

    @Test
    fun getOverrideResultForGlobalScriptedVariable_duplicate_returnsResult() {
        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.txt", "@var = 1")

        markFileInfo(gameType, "common/scripted_variables/99_scripted_variables.txt")
        myFixture.configureByText("99_scripted_variables.txt", "<caret>@var = 2")
        val variable = myFixture.findElementAtCaret()!!.parentOfType<ParadoxScriptScriptedVariable>()!!
        val result = ParadoxOverrideService.getOverrideResultForGlobalScriptedVariable(variable, myFixture.file)
        Assert.assertNotNull(result)
        Assert.assertEquals("var", result!!.key)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, result.overrideStrategy)
    }

    @Test
    fun getOverrideResultForGlobalScriptedVariable_singleOccurrence_returnsNull() {
        markFileInfo(gameType, "common/scripted_variables/01_scripted_variables.txt")
        myFixture.configureByText("01_scripted_variables.txt", "<caret>@var = 1")
        val variable = myFixture.findElementAtCaret()!!.parentOfType<ParadoxScriptScriptedVariable>()!!
        Assert.assertNull(ParadoxOverrideService.getOverrideResultForGlobalScriptedVariable(variable, myFixture.file))
    }

    // endregion

    // region getOverrideResultForDefineVariable Tests

    @Test
    fun getOverrideResultForDefineVariable_duplicate_returnsResult() {
        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                Variable = 1
            }
        """)

        markFileInfo(gameType, "common/defines/99_defines.txt")
        myFixture.configureByText("99_defines.txt", """
            Namespace = {
                <caret>Variable = 2
            }
        """)
        val property = myFixture.findElementAtCaret()!!.parentOfType<ParadoxScriptProperty>()!!
        val result = ParadoxOverrideService.getOverrideResultForDefineVariable(property, myFixture.file)
        Assert.assertNotNull(result)
        Assert.assertEquals(ParadoxOverrideStrategy.LIOS, result!!.overrideStrategy)
    }

    @Test
    fun getOverrideResultForDefineVariable_singleOccurrence_returnsNull() {
        markFileInfo(gameType, "common/defines/01_defines.txt")
        myFixture.configureByText("01_defines.txt", """
            Namespace = {
                <caret>Variable = 1
            }
        """)
        val property = myFixture.findElementAtCaret()!!.parentOfType<ParadoxScriptProperty>()!!
        Assert.assertNull(ParadoxOverrideService.getOverrideResultForDefineVariable(property, myFixture.file))
    }

    // endregion

    // region getOverrideResultForFile Tests

    @Test
    fun getOverrideResultForFile_samePath_returnsResult() {
        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.txt", "namespace = test")

        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.copy.txt", "namespace = test")
        val result = ParadoxOverrideService.getOverrideResultForFile(myFixture.file)
        Assert.assertNotNull(result)
        Assert.assertEquals(ParadoxOverrideStrategy.FIOS, result!!.overrideStrategy)
    }

    @Test
    fun getOverrideResultForFile_singleOccurrence_returnsNull() {
        markFileInfo(gameType, "events/01_test_events.txt")
        myFixture.configureByText("01_test_events.txt", "namespace = test")
        Assert.assertNull(ParadoxOverrideService.getOverrideResultForFile(myFixture.file))
    }

    // endregion

    // region isOverrideCorrect Tests

    @Test
    fun isOverrideCorrect_ordered_alwaysTrue() {
        val result = ParadoxOverrideResult(
            key = "my_action",
            target = configureProperty("common/on_actions/01_on_actions.txt", "01_on_actions.txt", "<caret>my_action = { events = { } }"),
            results = emptyList(),
            overrideStrategy = ParadoxOverrideStrategy.ORDERED,
        )
        Assert.assertTrue(ParadoxOverrideService.isOverrideCorrect(result))
    }

    // endregion

    // region Helpers

    private fun configureProperty(path: String, fileName: String, text: String): ParadoxScriptProperty {
        markFileInfo(gameType, path)
        myFixture.configureByText(fileName, text.trimIndent())
        return myFixture.findElementAtCaret()!!.parentOfType<ParadoxScriptProperty>()!!
    }

    // endregion
}



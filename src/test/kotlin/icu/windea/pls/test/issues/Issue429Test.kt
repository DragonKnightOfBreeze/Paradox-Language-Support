package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.common.UnusedParameterInspection
import icu.windea.pls.lang.resolve.util.ParadoxParameterSupportFactory
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#429](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/429)
 *
 * @see UnusedParameterInspection
 * @see ParadoxParameterSupportFactory.resolveParameterForDefinition
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue429Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/429")
        initConfigGroups(project, ParadoxGameType.Vic3)
        myFixture.enableInspections(UnusedParameterInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region declared normally

    // e.g., `com_change_and_clamp_variable = {...}`

    @Test
    fun testInspection_UsedParametersInScriptedEffect() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UsedParametersInScriptedEffect_Injected() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UsedParametersInScriptedEffect_Nested() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller_nested.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller_nested.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UsedParametersInScriptedEffect_NestedAndInjected() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller_nested_and_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller_nested_and_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    // endregion

    // region declared by definition injection

    // e.g., `REPLACE_OR_CREATE:com_change_and_clamp_variable = {...}`

    @Test
    fun testInspection_UsedParametersInScriptedEffect_FromInjection() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UsedParametersInScriptedEffect_FromInjection_Injected() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UsedParametersInScriptedEffect_FromInjection_Nested() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller_nested.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller_nested.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_UsedParametersInScriptedEffect_FromInjection_NestedAndInjected() {
        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/00_test_declaration_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/00_test_declaration_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/scripted_effects/01_test_caller_nested_and_injected.txt")
        myFixture.configureByFile("issues/429/common/scripted_effects/01_test_caller_nested_and_injected.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    // endregion
}

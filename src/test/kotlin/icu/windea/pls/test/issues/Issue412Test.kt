package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors as Colors

/**
 * See: [#412](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/412)
 *
 * Regression in 3.0.2: aliases declared via inline-script parameters (e.g., the argument of
 * `inline_script = { script = set_flag ... }`) were no longer resolved, with no highlighting and no
 * navigation to the declaration. In 3.0.1 the declaration was resolved correctly.
 *
 * Cause: the config context did not distinguish whether it is a declaration context (it was inferred only
 * from the context layer), so the parameter-related contexts inferred the wrong configs. A `declaration`
 * flag was introduced on the config context and the parameter support factory was updated accordingly.
 *
 * @see icu.windea.pls.lang.resolve.CwtConfigContext
 * @see icu.windea.pls.lang.resolve.util.ParadoxParameterSupportFactory
 * @see icu.windea.pls.lang.util.ParadoxParameterManager
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue412Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/412")
        markConfigDirectory("issues/412/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testSemanticAnnotator_ForScriptedEffectArgument() {
        val param = "\$FLAG$"
        val arg = "FLAG"

        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/00_scripted_effects.txt")
        myFixture.configureByText("00_scripted_effects.txt") {
            """
            base_effect_1 = {

            }
            base_effect_2 = {
                set_flag = ${param}
            }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/test.txt")
        myFixture.configureByText("test.txt") {
            """
            ${info(Colors.DEFINITION)}test_effect${infoEnd()} = {
                ${info(Colors.DEFINITION_REFERENCE)}base_effect_1${infoEnd()} = yes
                ${info(Colors.DEFINITION_REFERENCE)}base_effect_2${infoEnd()} = {
                    ${info(Colors.SEMANTIC_ARGUMENT)}${arg}${infoEnd()} = ${inject()}${info(Colors.DYNAMIC_VALUE)}some_flag${infoEnd()}${injectEnd()}
                }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting(false, true, false)
    }

    @Test
    fun testSemanticAnnotator_ForInlineScriptArgument() {
        val param = "\$FLAG$"
        val arg = "FLAG"

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/set_flag.txt")
        myFixture.configureByText("set_flag.txt") {
            """
            set_flag = ${param}
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/test.txt")
        myFixture.configureByText("test.txt") {
            """
            ${info(Colors.DEFINITION)}test_effect${infoEnd()} = {
                ${info(Colors.MACRO)}inline_script${infoEnd()} = {
                    script = ${info(Colors.PATH_REFERENCE)}set_flag${infoEnd()}
                    ${info(Colors.SEMANTIC_ARGUMENT)}${arg}${infoEnd()} = ${inject()}${info(Colors.DYNAMIC_VALUE)}some_flag${infoEnd()}${injectEnd()}
                }
            }
            """.trimIndent()
        }
        myFixture.checkHighlighting(false, true, false)
    }

    @Test
    fun testSemanticAnnotator_InInlineScriptFile() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/test.txt")
        myFixture.configureByText("test.txt") {
            """
            test_effect = {
                inline_script =  set_flag
            }
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/set_flag.txt")
        myFixture.configureByText("set_flag.txt") {
            """
            ${info(Colors.EFFECT)}set_flag${infoEnd()} = ${info(Colors.DYNAMIC_VALUE)}some_flag${infoEnd()}
            """.trimIndent()
        }

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting(false, true, false)
    }
}

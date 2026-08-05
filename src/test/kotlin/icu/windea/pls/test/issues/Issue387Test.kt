package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertIs

/**
 * See: [#387](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/387)
 *
 * @see ParadoxCommandExpression
 * @see ParadoxCommandFieldValueNode
 * @see ParadoxCommandFieldValueNode.resolveDsNode
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue387Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/387")
        markConfigDirectory("issues/387/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testReferenceResolution() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_loc/00_dk_scripted_loc.txt")
        myFixture.configureByText("00_dk_scripted_loc.txt", """
            defined_text = {
            	name = GetDragonKnightTitle
            	text = {
            		trigger = {
            			has_character_flag = dragon_tamed
            		}
            		localization_key = dk_breeze
            	}
            }
        """.trimIndent())

        markFileInfo(ParadoxGameType.Stellaris, "localisation/00_dk_l_simp_chinese.yml")
        myFixture.configureByText("00_dk_l_simp_chinese.yml", """
            l_english:
             intro: "From Windea, the Dragon Knight of Breeze."
             windea: "Windea"
             dk_breeze: "the Dragon Knight of Breeze"
             the_dragon_name: "<secret>"
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Stellaris, "localisation/01_dk_l_simp_chinese.yml")
        myFixture.configureByText("01_dk_l_simp_chinese.yml", """
            l_english:
             intro: "From [<caret>GetName], [GetDragonKnightTitle]."
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxStaticCommandFieldNode.Reference>(reference)
            val resolved = reference.resolve()!!
            assertTrue(resolved.name == "GetName")
        }

        markFileInfo(ParadoxGameType.Stellaris, "localisation/01_dk_l_simp_chinese.yml")
        myFixture.configureByText("01_dk_l_simp_chinese.yml", """
            l_english:
             intro: "From [GetName], [<caret>GetDragonKnightTitle]."
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxDataSourceNode.Reference>(reference)
            val resolved = reference.resolve()!!
            assertIs<ParadoxScriptProperty>(resolved)
            assertTrue(resolved.name == "defined_text")
            val definitionInfo = resolved.definitionInfo!!
            assertTrue(definitionInfo.name == "GetDragonKnightTitle" && definitionInfo.type == "scripted_loc" && definitionInfo.typeKey == "defined_text")
        }

        markFileInfo(ParadoxGameType.Stellaris, "localisation/02_dk_l_simp_chinese.yml")
        myFixture.configureByText("02_dk_l_simp_chinese.yml", """
            l_english:
             intro: "From Windea, the curious seeker who has tamed the dragon named [<caret>the_dragon_name]."
        """.trimIndent())
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxDataSourceNode.Reference>(reference)
            val resolved = reference.resolve()!!
            assertIs<ParadoxDynamicValueLightElement>(resolved)
            assertTrue(resolved.name == "the_dragon_name" && resolved.presentableType == "variable")
        }

        markFileInfo(ParadoxGameType.Stellaris, "localisation/03_dk_l_simp_chinese.yml")
        myFixture.configureByText("03_dk_l_simp_chinese.yml", """
            l_english:
             intro: "From Windea, the curious seeker who has tamed the dragon named [<caret>the_dragon_name@root]."
        """.trimIndent())
        run {
            val reference = myFixture.findReferenceAtCaret()!!
            assertIs<ParadoxDynamicValueNode.Reference>(reference)
            val resolved = reference.resolve()!!
            assertIs<ParadoxDynamicValueLightElement>(resolved)
            assertTrue(resolved.name == "the_dragon_name" && resolved.presentableType == "variable")
        }
    }
}

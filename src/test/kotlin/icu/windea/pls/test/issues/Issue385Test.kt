package icu.windea.pls.test.issues

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.expression.ConflictingResolvedExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.IncorrectExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.MissingExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.TooManyExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.modifierConfig
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#385](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/385)
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue385Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/385")
        markConfigDirectory("issues/385/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testInspection_ForModifierNames() {
        enableAllNeededInspections()

        markFileInfo(ParadoxGameType.Stellaris, "common/weapons/00_weapons.txt")
        myFixture.configureByFile("issues/385/common/weapons/00_weapons.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/weapons/types/00_weapon_types.txt")
        myFixture.configureByFile("issues/385/common/weapons/types/00_weapon_types.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            test = {
                modifier = weapon_damage_mult
                modifier = weapon_anti_evil_saber_damage_mult
                modifier = weapon_windea_long_sword_damage_mult
                modifier = weapon_type_sword_damage_mult
                modifier = weapon_type_rifle_damage_mult
                modifier = weapon_tag_special_damage_mult
                modifier = weapon_tag_spirit_art_damage_mult

                modifier = weapon_magic_power_mult
                modifier = weapon_guidance_for_Seekers_magic_power_mult
                modifier = weapon_ode_to_THE_DRAGON_KNIGHT_magic_power_mult
                modifier = weapon_type_BOOK_magic_power_Mult
                modifier = WEAPON_TAG_ELEMENTAL_ART_CASTER_MAGIC_POWER_MULT
                modifier = Weapon_Tag_Spirit_Art_Caster_Magic_Power_Mult
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testReferenceResolution_ForModifierNames() {
        enableAllNeededInspections()

        markFileInfo(ParadoxGameType.Stellaris, "common/weapons/00_weapons.txt")
        myFixture.configureByFile("issues/385/common/weapons/00_weapons.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/weapons/types/00_weapon_types.txt")
        myFixture.configureByFile("issues/385/common/weapons/types/00_weapon_types.txt")

        markFileInfo(ParadoxGameType.Stellaris, "common/test/test_1.txt")
        myFixture.configureByText("test_1.txt", """
            test = {
                modifier = <caret>weapon_windea_long_sword_damage_mult
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            reference.expectIs<ParadoxScriptExpressionPsiReference>()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxModifierLightElement>()
            resolved.name.expectEquals("weapon_windea_long_sword_damage_mult")
            val modifierConfig = resolved.modifierConfig.expectNotNull()
            modifierConfig.name.expectEquals("weapon_<weapon>_damage_mult")
        }

        markFileInfo(ParadoxGameType.Stellaris, "common/test/test_2.txt")
        myFixture.configureByText("test_2.txt", """
            test = {
                modifier = <caret>weapon_ode_to_THE_DRAGON_KNIGHT_magic_power_mult
            }
        """.trimIndent())
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        expectScope {
            val reference = myFixture.findReferenceAtCaret().expectNotNull()
            reference.expectIs<ParadoxScriptExpressionPsiReference>()
            val resolved = reference.resolve().expectNotNull()
            resolved.expectIs<ParadoxModifierLightElement>()
            resolved.name.expectEquals("weapon_ode_to_THE_DRAGON_KNIGHT_magic_power_mult")
            val modifierConfig = resolved.modifierConfig.expectNotNull()
            modifierConfig.name.expectEquals("weapon_<weapon>_magic_power_mult")
        }
    }

    private fun enableAllNeededInspections() {
        // enable all needed expression inspections
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
        myFixture.enableInspections(ConflictingResolvedExpressionInspection::class.java)
        myFixture.enableInspections(MissingExpressionInspection::class.java)
        myFixture.enableInspections(TooManyExpressionInspection::class.java)
        myFixture.enableInspections(IncorrectExpressionInspection::class.java)
    }
}

package icu.windea.pls.test.issues

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.select.one
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#458](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/458)
 *
 * Localisation links for ship section templates (`ship_section_template` in
 * `common/section_templates`) were lost: no localisation hints/quick doc on the definition side,
 * and the localisation could not index back to the section definition.
 *
 * The Stellaris rules declare section template localisation only under the `needs_locs` subtype,
 * whose selector requires the referenced `ship_size` definition to be `designable`:
 *
 * ```cwt
 * subtype[needs_locs] = { ship_size = <ship_size.designable> }
 * localisation = { subtype[needs_locs] = { name = "$" } }
 * ```
 *
 * Cause:
 * - `ParadoxDefinitionInfo` eagerly initialized its subtype-dependent derived properties
 *   (`localisations`, `images`, `modifiers`, `primaryLocalisations`, `primaryImages`) in the
 *   constructor.
 * - Its `element` is assigned by `ParadoxDefinitionService` only after construction, so these
 *   properties computed subtypes from an empty element and stayed permanently empty.
 * - Types whose localisation is declared only under a subtype (like `section_template`'s
 *   `needs_locs`) therefore lost their localisation links in both directions.
 *
 * @see icu.windea.pls.model.ParadoxDefinitionInfo
 * @see icu.windea.pls.lang.resolve.ParadoxDefinitionService
 * @see icu.windea.pls.lang.resolve.ParadoxLocalisationService
 * @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue458Test : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/458")
        markConfigDirectory("issues/458/.config")
        initInjectedConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region definition side

    @Test
    fun testDefinitionInfo_SectionTemplate() {
        val section = setUpSectionTemplate()

        expectScope {
            val info = section.definitionInfo.expectNotNull()
            info.type.expectEquals("section_template")
            info.name.expectEquals("lglr_tundra_a")
            "needs_locs".expectIn(info.subtypes)
            "name".expectIn(info.localisations.map { it.key })
        }
    }

    @Test
    fun testDefinition_RelatedLocalisations() {
        val section = setUpSectionTemplate()
        setUpLocalisation()

        expectScope {
            val related = ParadoxDefinitionManager.getRelatedLocalisations(section, englishLocale, onlyOne = false)
            "lglr_tundra_a".expectIn(related.map { it.name })
        }
        expectScope {
            val primaryRelated = ParadoxDefinitionManager.getRelatedLocalisations(section, englishLocale, onlyOne = false, onlyPrimary = true)
            "lglr_tundra_a".expectIn(primaryRelated.map { it.name })
        }
    }

    @Test
    fun testDefinition_PrimaryLocalisationKey() {
        val section = setUpSectionTemplate()
        setUpLocalisation()

        expectScope {
            ParadoxDefinitionManager.getPrimaryLocalisationKey(section).expectEquals("lglr_tundra_a")
        }
    }

    // endregion

    // region localisation side

    @Test
    fun testLocalisation_RelatedDefinitions() {
        val section = setUpSectionTemplate()
        setUpLocalisation()

        val localisation = findLocalisation()
        expectScope {
            val related = ParadoxLocalisationManager.getRelatedDefinitions(localisation)
            "lglr_tundra_a".expectIn(related.map { it.definitionInfo?.name })
            section.expectIn(related)
        }
    }

    // endregion

    // region helpers

    private val englishLocale: CwtLocaleConfig
        get() = ChronicleFacade.getConfigGroup(project, gameType).locales.getValue("l_english")

    private fun setUpSectionTemplate(): ParadoxScriptProperty {
        markFileInfo(gameType, "common/ship_sizes/00_ship_sizes.txt")
        myFixture.configureByFile("issues/458/common/ship_sizes/00_ship_sizes.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(gameType, "common/section_templates/battleship.txt")
        myFixture.configureByFile("issues/458/common/section_templates/battleship.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val file = myFixture.file as ParadoxScriptFile
        return selectScope { file.queryBy("ship_section_template").asProperty().one() }!!
    }

    private fun setUpLocalisation() {
        markFileInfo(gameType, "localisation/english/ship_sections_l_english.yml")
        myFixture.configureByFile("issues/458/localisation/english/ship_sections_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    private fun findLocalisation(): ParadoxLocalisationProperty {
        return PsiTreeUtil.findChildrenOfType(myFixture.file, ParadoxLocalisationProperty::class.java)
            .first { it.name == "lglr_tundra_a" }
    }

    // endregion
}

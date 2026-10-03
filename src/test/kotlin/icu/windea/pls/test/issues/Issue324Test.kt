package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.ExpectScope.expectOrderedEquals
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#324](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/324)
 *
 * Nested subtypes were not supported in type localisation (and presentation) configs: a `subtype[x] = {...}`
 * nested inside another `subtype[x] = {...}` was not resolved, so the related localisation of the nested
 * subtype was missing.
 *
 * Cause: the resolver only handled a single level of `subtype[x] = {...}` and matched the plain location
 * rules directly, without flattening the nested subtype expressions first. The subtype expressions are now
 * flattened before grouping the location configs by subtype expression recursively.
 *
 * @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig
 * @see icu.windea.pls.config.manipulation.CwtConfigExpansionService.expandBySubtypeExpression
 * @see icu.windea.pls.lang.resolve.ParadoxDefinitionService.resolveRelatedLocalisationInfos
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue324Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/324")
        markConfigDirectory("issues/324/.config")
        initConfigGroups(project)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testWeapons() {
        val configGroup = ChronicleFacade.getConfigGroup(myFixture.project, ParadoxGameType.Core)

        val typeConfig = configGroup.types["weapon"]
        assertNotNull(typeConfig)
        typeConfig!!

        val typeLocalisationConfig = typeConfig.localisation
        assertNotNull(typeLocalisationConfig)
        typeLocalisationConfig!!

        expectScope {
            val r = typeLocalisationConfig.getLocationConfigs(emptyList()).map { it.key }
            r.expectOrderedEquals("name", "desc")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("blade_weapon", "sword")).map { it.key }
            r.expectOrderedEquals("name", "desc", "blade_attack_desc", "sword_size")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("blade_weapon", "spear")).map { it.key }
            r.expectOrderedEquals("name", "desc", "blade_attack_desc", "spear_size")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("strike_weapon", "whip")).map { it.key }
            r.expectOrderedEquals("name", "desc", "strike_attack_desc")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("ranged_weapon", "bow")).map { it.key }
            r.expectOrderedEquals("name", "desc", "default_arrow", "bonus_desc")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("ranged_weapon", "crossbow")).map { it.key }
            r.expectOrderedEquals("name", "desc", "default_arrow")
        }
    }
}

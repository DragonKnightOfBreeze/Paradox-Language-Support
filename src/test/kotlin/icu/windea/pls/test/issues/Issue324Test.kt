package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#324](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/324)
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue324Test : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Core

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/324")
        markConfigDirectory("issues/324/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testWeapons() {
        val configGroup = ChronicleFacade.getConfigGroup(myFixture.project)

        val typeConfig = configGroup.types["weapon"]
        assertNotNull(typeConfig)
        typeConfig!!

        val typeLocalisationConfig = typeConfig.localisation
        assertNotNull(typeLocalisationConfig)
        typeLocalisationConfig!!

        run {
            val r = typeLocalisationConfig.getLocationConfigs(emptyList()).map { it.key }
            assertSameElements(r, "name", "desc")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("blade_weapon", "sword")).map { it.key }
            assertSameElements(r, "name", "desc", "blade_attack_desc", "sword_size")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("blade_weapon", "spear")).map { it.key }
            assertSameElements(r, "name", "desc", "blade_attack_desc", "spear_size")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("strike_weapon", "whip")).map { it.key }
            assertSameElements(r, "name", "desc", "strike_attack_desc")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("ranged_weapon", "bow")).map { it.key }
            assertSameElements(r, "name", "desc", "default_arrow", "bonus_desc")
        }
        run {
            val r = typeLocalisationConfig.getLocationConfigs(listOf("ranged_weapon", "crossbow")).map { it.key }
            assertSameElements(r, "name", "desc", "default_arrow")
        }
    }
}

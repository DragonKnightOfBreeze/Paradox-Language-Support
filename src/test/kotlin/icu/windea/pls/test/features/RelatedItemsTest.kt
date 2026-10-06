package icu.windea.pls.test.features

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.select.one
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * relatedItems（相关项）的回归测试。
 *
 * 使用自行编写的规则文件、脚本文件、本地化文件和（占位符）图片文件（均位于 `feature/relatedItems`），
 * 覆盖各种目标（定义、封装变量、复杂枚举值、动态值、修正）的相关本地化与相关图片，并着重对覆盖顺序
 * （`onlyOne = true` 与 `onlyOne = false`）进行冒烟测试。
 *
 * @see ParadoxDefinitionManager
 * @see ParadoxScriptedVariableManager
 * @see ParadoxComplexEnumValueManager
 * @see ParadoxDynamicValueManager
 * @see ParadoxModifierManager
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class RelatedItemsTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/relatedItems")
        markConfigDirectory("features/relatedItems/.config")
        initInjectedConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region definition

    @Test
    fun definition_relatedLocalisations_all() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val related = ParadoxDefinitionManager.getRelatedLocalisations(flameBlade, englishLocale, onlyOne = false, onlyPrimary = true)
            related.map { it.containingFile.name }.expectOrderedEquals(
                "01_weapons_l_english.yml", // 同一语言环境下，按覆盖顺序，后到者优先
                "00_weapons_l_english.yml",
                "00_weapons_l_simp_chinese.yml", // 其他语言环境排在偏好语言环境之后
            )
        }
    }

    @Test
    fun definition_relatedLocalisations_onlyOne() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val related = ParadoxDefinitionManager.getRelatedLocalisations(flameBlade, englishLocale, onlyOne = true, onlyPrimary = true)
            related.map { it.containingFile.name }.expectOrderedEquals("01_weapons_l_english.yml")
        }
    }

    @Test
    fun definition_relatedLocalisations_preferredLocale() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val related = ParadoxDefinitionManager.getRelatedLocalisations(flameBlade, chineseLocale, onlyOne = false, onlyPrimary = true)
            related.map { it.containingFile.name }.expectOrderedEquals(
                "00_weapons_l_simp_chinese.yml", // 偏好语言环境被置顶
                "01_weapons_l_english.yml",
                "00_weapons_l_english.yml",
            )
        }
    }

    @Test
    fun definition_relatedLocalisations_nonPrimaryIncluded() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val related = ParadoxDefinitionManager.getRelatedLocalisations(flameBlade, englishLocale, onlyOne = false, onlyPrimary = false)
            // name（主键）在前，desc（非主键）在后
            related.map { it.name }.expectOrderedEquals(
                "flame_blade", "flame_blade", "flame_blade",
                "flame_blade_desc", "flame_blade_desc", "flame_blade_desc",
            )
        }
    }

    @Test
    fun definition_relatedImages_primary() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val images = ParadoxDefinitionManager.getRelatedImages(flameBlade, onlyOne = false, onlyPrimary = true)
            images.map { it.name }.expectOrderedEquals("flame_blade.dds")
        }
    }

    @Test
    fun definition_relatedImages_all() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val images = ParadoxDefinitionManager.getRelatedImages(flameBlade, onlyOne = false, onlyPrimary = false)
            images.map { it.name }.expectOrderedEquals("flame_blade.dds", "flame_blade_portrait.dds")
        }
    }

    @Test
    fun definition_relatedImages_onlyOne() {
        val file = setUpData()
        val flameBlade = selectScope { file.queryBy("flame_blade").asProperty().one() }!!

        expectScope {
            val images = ParadoxDefinitionManager.getRelatedImages(flameBlade, onlyOne = true, onlyPrimary = false)
            images.map { it.name }.expectOrderedEquals("flame_blade.dds")
        }
    }

    // endregion

    // region scripted variable

    @Test
    fun scriptedVariable_relatedLocalisations() {
        setUpData()

        expectScope {
            val related = ParadoxScriptedVariableManager.getRelatedLocalisations("arcane_surge", myFixture.file, englishLocale)
            related.map { it.name }.expectOrderedEquals("arcane_surge")
        }
        expectScope {
            val related = ParadoxScriptedVariableManager.getRelatedLocalisations("arcane_surge", myFixture.file, englishLocale, onlyOne = true)
            related.map { it.name }.expectOrderedEquals("arcane_surge")
        }
    }

    // endregion

    // region complex enum value

    @Test
    fun complexEnumValue_relatedLocalisations() {
        setUpData()

        expectScope {
            val related = ParadoxComplexEnumValueManager.getRelatedLocalisations("school_of_evocation", myFixture.file, englishLocale)
            related.map { it.name }.expectOrderedEquals("school_of_evocation")
        }
        expectScope {
            val related = ParadoxComplexEnumValueManager.getRelatedLocalisations("school_of_evocation", myFixture.file, englishLocale, onlyOne = true)
            related.map { it.name }.expectOrderedEquals("school_of_evocation")
        }
    }

    // endregion

    // region dynamic value

    @Test
    fun dynamicValue_relatedLocalisations() {
        setUpData()

        expectScope {
            val related = ParadoxDynamicValueManager.getRelatedLocalisations("home_plane", myFixture.file, englishLocale)
            related.map { it.name }.expectOrderedEquals("home_plane")
        }
        expectScope {
            val related = ParadoxDynamicValueManager.getRelatedLocalisations("home_plane", myFixture.file, englishLocale, onlyOne = true)
            related.map { it.name }.expectOrderedEquals("home_plane")
        }
    }

    // endregion

    // region modifier

    @Test
    fun modifier_relatedLocalisations_all() {
        setUpData()

        expectScope {
            val related = ParadoxModifierManager.getRelatedLocalisations(
                "flame_damage_mult", myFixture.file, englishLocale, onlyOne = false, forName = true, forDesc = false,
            )
            related.map { it.containingFile.name }.expectOrderedEquals(
                "01_modifiers_l_english.yml", // 同一语言环境下，按覆盖顺序，后到者优先
                "00_modifiers_l_english.yml",
                "00_modifiers_l_simp_chinese.yml", // 其他语言环境排在偏好语言环境之后
            )
        }
    }

    @Test
    fun modifier_relatedLocalisations_onlyOne() {
        setUpData()

        expectScope {
            val related = ParadoxModifierManager.getRelatedLocalisations(
                "flame_damage_mult", myFixture.file, englishLocale, onlyOne = true, forName = true, forDesc = false,
            )
            // 仅返回一个本地化
            related.size.expectEquals(1)
            related.first().containingFile.name.expectIn(
                listOf("01_modifiers_l_english.yml", "00_modifiers_l_english.yml"),
            )
        }
    }

    @Test
    fun modifier_relatedImages() {
        setUpData()

        expectScope {
            val images = ParadoxModifierManager.getRelatedImages("flame_damage_mult", myFixture.file)
            images.map { it.name }.expectUnorderedEquals("mod_flame_damage_mult.dds")
        }
    }

    // endregion

    // region Helpers

    private val englishLocale: CwtLocaleConfig
        get() = ChronicleFacade.getConfigGroup(project, gameType).locales.getValue("l_english")

    private val chineseLocale: CwtLocaleConfig
        get() = ChronicleFacade.getConfigGroup(project, gameType).locales.getValue("l_simp_chinese")

    /**
     * 配置本测试所需的全部测试数据文件，并以（最后一个配置的）武器脚本文件作为上下文。
     */
    private fun setUpData(): ParadoxScriptFile {
        configureFile("features/relatedItems/localisation/00_weapons_l_english.yml")
        configureFile("features/relatedItems/localisation/01_weapons_l_english.yml")
        configureFile("features/relatedItems/localisation/00_weapons_l_simp_chinese.yml")
        configureFile("features/relatedItems/localisation/00_armor_l_english.yml")
        configureFile("features/relatedItems/localisation/00_spells_l_english.yml")
        configureFile("features/relatedItems/localisation/00_modifiers_l_english.yml")
        configureFile("features/relatedItems/localisation/01_modifiers_l_english.yml")
        configureFile("features/relatedItems/localisation/00_modifiers_l_simp_chinese.yml")
        configureFile("features/relatedItems/localisation/00_targets_l_english.yml")

        configureImage("features/relatedItems/gfx/interface/icons/weapons/flame_blade.dds")
        configureImage("features/relatedItems/gfx/interface/icons/weapons/flame_blade_portrait.dds")
        configureImage("features/relatedItems/gfx/interface/icons/weapons/frost_hammer.dds")
        configureImage("features/relatedItems/gfx/interface/icons/armor/dragon_scale.dds")
        configureImage("features/relatedItems/gfx/interface/icons/modifiers/mod_flame_damage_mult.dds")
        configureImage("features/relatedItems/gfx/interface/icons/modifiers/mod_frost_resistance_mult.dds")

        configureFile("features/relatedItems/common/weapons/00_weapons.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        return myFixture.file as ParadoxScriptFile
    }

    private fun configureFile(@TestDataFile filePath: String) {
        markFileInfo(gameType, filePath.removePrefix("features/relatedItems/"))
        myFixture.configureByFile(filePath)
    }

    private fun configureImage(@TestDataFile filePath: String) {
        val copied = myFixture.copyFileToProject(filePath)
        copied.injectFileInfo(gameType, filePath.removePrefix("features/relatedItems/"))
        FileBasedIndex.getInstance().requestReindex(copied)
    }

    // endregion
}

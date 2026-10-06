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
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * presentableItems（展示项，这里主要是展示名字）的回归测试。
 *
 * 使用自行编写的规则文件、脚本文件、本地化文件和（占位符）图片文件（均位于 `features/relatedItems`），
 * 覆盖各种目标（定义、封装变量、复杂枚举值、动态值、修正）的展示名字，并着重对覆盖顺序
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
class PresentableItemsTest : BasePlatformTestCase(), ChronicleTestScope {
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
    fun definition_presentableNames_all() {
        val file = setUpData()
        val flameBlade = file.findProperty("flame_blade")

        expectScope {
            val names = ParadoxDefinitionManager.getPresentableNames(flameBlade, englishLocale, onlyOne = false)
            names.expectOrderedEquals("Flame Blade (Reforged)", "Flame Blade", "烈焰之刃")
        }
    }

    @Test
    fun definition_presentableNames_onlyOne() {
        val file = setUpData()
        val flameBlade = file.findProperty("flame_blade")

        expectScope {
            val names = ParadoxDefinitionManager.getPresentableNames(flameBlade, englishLocale, onlyOne = true)
            names.expectOrderedEquals("Flame Blade (Reforged)")
        }
    }

    @Test
    fun definition_presentableNames_preferredLocale() {
        val file = setUpData()
        val flameBlade = file.findProperty("flame_blade")

        expectScope {
            val names = ParadoxDefinitionManager.getPresentableNames(flameBlade, chineseLocale, onlyOne = false)
            names.expectOrderedEquals("烈焰之刃", "Flame Blade (Reforged)", "Flame Blade")
        }
    }

    // endregion

    // region scripted variable

    @Test
    fun scriptedVariable_presentableNames() {
        setUpData()

        expectScope {
            val names = ParadoxScriptedVariableManager.getPresentableNames("arcane_surge", myFixture.file, englishLocale)
            names.expectOrderedEquals("Arcane Surge")
        }
        expectScope {
            val names = ParadoxScriptedVariableManager.getPresentableNames("arcane_surge", myFixture.file, englishLocale, onlyOne = true)
            names.expectOrderedEquals("Arcane Surge")
        }
    }

    // endregion

    // region complex enum value

    @Test
    fun complexEnumValue_presentableNames() {
        setUpData()

        expectScope {
            val names = ParadoxComplexEnumValueManager.getPresentableNames("school_of_evocation", myFixture.file, englishLocale)
            names.expectOrderedEquals("School of Evocation")
        }
        expectScope {
            val names = ParadoxComplexEnumValueManager.getPresentableNames("school_of_evocation", myFixture.file, englishLocale, onlyOne = true)
            names.expectOrderedEquals("School of Evocation")
        }
    }

    // endregion

    // region dynamic value

    @Test
    fun dynamicValue_presentableNames() {
        setUpData()

        expectScope {
            val names = ParadoxDynamicValueManager.getPresentableNames("home_plane", myFixture.file, englishLocale)
            names.expectOrderedEquals("Home Plane")
        }
        expectScope {
            val names = ParadoxDynamicValueManager.getPresentableNames("home_plane", myFixture.file, englishLocale, onlyOne = true)
            names.expectOrderedEquals("Home Plane")
        }
    }

    // endregion

    // region modifier

    @Test
    fun modifier_presentableNames_all() {
        setUpData()

        expectScope {
            val names = ParadoxModifierManager.getPresentableNames("flame_damage_mult", myFixture.file, englishLocale, onlyOne = false)
            names.expectOrderedEquals("Flame Damage (Reforged)", "Flame Damage", "火焰伤害")
        }
    }

    @Test
    fun modifier_presentableNames_onlyOne() {
        setUpData()

        expectScope {
            val names = ParadoxModifierManager.getPresentableNames("flame_damage_mult", myFixture.file, englishLocale, onlyOne = true)
            // 仅返回一个展示名字
            names.size.expectEquals(1)
            names.first().expectIn(listOf("Flame Damage (Reforged)", "Flame Damage"))
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

    private fun ParadoxScriptFile.findProperty(name: String): ParadoxScriptProperty {
        return selectScope { this@findProperty.queryBy(name).asProperty().one() }!!
    }

    // endregion
}

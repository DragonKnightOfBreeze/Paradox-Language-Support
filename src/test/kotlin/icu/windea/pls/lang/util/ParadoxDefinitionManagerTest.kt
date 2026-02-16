package icu.windea.pls.lang.util

import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefinitionManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/resolve")
        markConfigDirectory("features/resolve/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun clear() = clearIntegrationTest()

    private fun configureScriptFile(relPath: String, @TestDataFile testDataPath: String): ParadoxScriptFile {
        markFileInfo(ParadoxGameType.Stellaris, relPath)
        myFixture.configureByFile(testDataPath)
        return myFixture.file as ParadoxScriptFile
    }

    // region Basic Property Definition

    @Test
    fun testGetInfo_BasicProperty() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // 获取 titan_mk3 定义
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)
        Assert.assertNotNull(titanInfo)
        titanInfo!!

        Assert.assertEquals("titan_mk3", titanInfo.name)
        Assert.assertEquals("mech", titanInfo.type)
        Assert.assertEquals("titan_mk3", titanInfo.typeKey)
        Assert.assertEquals(ParadoxDefinitionSource.Property, titanInfo.source)
        Assert.assertEquals(ParadoxGameType.Stellaris, titanInfo.gameType)
        Assert.assertEquals(listOf<String>(), titanInfo.rootKeys)
    }

    @Test
    fun testGetInfo_BasicProperty_MultipleDefinitions() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        val definitions = selectScope { file.properties().asProperty().all() }
        Assert.assertEquals(3, definitions.size)

        val names = definitions.mapNotNull { prop -> ParadoxDefinitionManager.getInfo(prop)?.name }.toSet()
        Assert.assertEquals(setOf("titan_mk3", "phantom", "vanguard"), names)
    }

    // endregion

    // region Subtypes

    @Test
    fun testSubtypes_Matched() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // titan_mk3 有 weight_class = heavy，应匹配子类型 "heavy"
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!
        Assert.assertEquals("mech", titanInfo.type)
        Assert.assertEquals(listOf("heavy"), titanInfo.subtypes)
        Assert.assertEquals(listOf("mech", "heavy"), titanInfo.types)
        Assert.assertEquals("mech, heavy", titanInfo.typeText)
    }

    @Test
    fun testSubtypes_DifferentSubtype() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // phantom 有 cloaking = yes，应匹配子类型 "stealth"
        val phantom = selectScope { file.ofPath("phantom").asProperty().one() }!!
        val phantomInfo = ParadoxDefinitionManager.getInfo(phantom)!!
        Assert.assertEquals("mech", phantomInfo.type)
        Assert.assertEquals(listOf("stealth"), phantomInfo.subtypes)
        Assert.assertEquals(listOf("mech", "stealth"), phantomInfo.types)
        Assert.assertEquals("mech, stealth", phantomInfo.typeText)
    }

    @Test
    fun testSubtypes_NoSubtype() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // vanguard 没有匹配任何子类型条件
        val vanguard = selectScope { file.ofPath("vanguard").asProperty().one() }!!
        val vanguardInfo = ParadoxDefinitionManager.getInfo(vanguard)!!
        Assert.assertEquals("mech", vanguardInfo.type)
        Assert.assertTrue(vanguardInfo.subtypes.isEmpty())
        Assert.assertEquals(listOf("mech"), vanguardInfo.types)
        Assert.assertEquals("mech", vanguardInfo.typeText)
    }

    @Test
    fun testSubtypes_MultipleSubtypeConfigs() {
        val file = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")

        val plasma = selectScope { file.ofPath("plasma_cannon").asProperty().one() }!!
        val plasmaInfo = ParadoxDefinitionManager.getInfo(plasma)!!
        Assert.assertEquals("weapon", plasmaInfo.type)
        Assert.assertEquals(listOf("energy"), plasmaInfo.subtypes)
        Assert.assertEquals(listOf("weapon", "energy"), plasmaInfo.types)
        Assert.assertEquals("weapon, energy", plasmaInfo.typeText)

        val railgun = selectScope { file.ofPath("railgun").asProperty().one() }!!
        val railgunInfo = ParadoxDefinitionManager.getInfo(railgun)!!
        Assert.assertEquals("weapon", railgunInfo.type)
        Assert.assertEquals(listOf("kinetic"), railgunInfo.subtypes)
        Assert.assertEquals(listOf("weapon", "kinetic"), railgunInfo.types)
        Assert.assertEquals("weapon, kinetic", railgunInfo.typeText)

        // pulse_blade 没有 damage_type 字段，不匹配任何子类型
        val pulseBlade = selectScope { file.ofPath("pulse_blade").asProperty().one() }!!
        val pulseBladeInfo = ParadoxDefinitionManager.getInfo(pulseBlade)!!
        Assert.assertEquals("weapon", pulseBladeInfo.type)
        Assert.assertTrue(pulseBladeInfo.subtypes.isEmpty())
        Assert.assertEquals(listOf("weapon"), pulseBladeInfo.types)
        Assert.assertEquals("weapon", pulseBladeInfo.typeText)
    }

    // endregion

    // region Localisation and Images

    @Test
    fun testLocalisations() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!

        // mech 类型定义了 localisation: name = "$", desc = "$_desc"
        Assert.assertEquals(2, titanInfo.localisations.size)
        Assert.assertEquals(setOf("name", "desc"), titanInfo.localisations.map { it.key }.toSet())
    }

    @Test
    fun testImages() {
        val file = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")

        val plasma = selectScope { file.ofPath("plasma_cannon").asProperty().one() }!!
        val plasmaInfo = ParadoxDefinitionManager.getInfo(plasma)!!

        // weapon 类型定义了 images: icon (primary) 和 portrait (optional)
        Assert.assertEquals(2, plasmaInfo.images.size)
        val iconImage = plasmaInfo.images.find { it.key == "icon" }
        Assert.assertNotNull(iconImage)
        Assert.assertTrue(iconImage!!.primary)
    }

    @Test
    fun testPrimaryLocalisations() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!

        // "name" 应被推断为 primary（通过 primaryByInference）
        val primaryLocs = titanInfo.primaryLocalisations
        Assert.assertTrue(primaryLocs.isNotEmpty())
        Assert.assertTrue(primaryLocs.any { it.key == "name" })
    }

    @Test
    fun testPrimaryImages() {
        val file = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")

        val plasma = selectScope { file.ofPath("plasma_cannon").asProperty().one() }!!
        val plasmaInfo = ParadoxDefinitionManager.getInfo(plasma)!!

        // "icon" 被标记为 primary 或通过推断为 primary
        val primaryImgs = plasmaInfo.primaryImages
        Assert.assertTrue(primaryImgs.isNotEmpty())
        Assert.assertTrue(primaryImgs.any { it.key == "icon" })
    }

    @Test
    fun testNoLocalisationsOrImages() {
        val file = configureScriptFile("common/signals/00_signals.txt", "features/resolve/common/signals/00_signals.txt")

        val distress = selectScope { file.ofPath("distress_beacon").asProperty().one() }!!
        val distressInfo = ParadoxDefinitionManager.getInfo(distress)!!

        // signal 类型没有定义 localisation 和 images
        Assert.assertTrue(distressInfo.localisations.isEmpty())
        Assert.assertTrue(distressInfo.images.isEmpty())
        Assert.assertTrue(distressInfo.primaryLocalisations.isEmpty())
        Assert.assertTrue(distressInfo.primaryImages.isEmpty())
    }

    // endregion

    // region Name Field

    @Test
    fun testNameField() {
        val file = configureScriptFile("common/pilots/00_pilots.txt", "features/resolve/common/pilots/00_pilots.txt")

        // pilot 类型使用 name_field = "callsign"，名字来自 callsign 字段
        val ace = selectScope { file.ofPath("ace").asProperty().one() }!!
        val aceInfo = ParadoxDefinitionManager.getInfo(ace)!!
        Assert.assertEquals("maverick", aceInfo.name)
        Assert.assertEquals("ace", aceInfo.typeKey) // typeKey 仍然是属性键

        val rookie = selectScope { file.ofPath("rookie").asProperty().one() }!!
        val rookieInfo = ParadoxDefinitionManager.getInfo(rookie)!!
        Assert.assertEquals("spark", rookieInfo.name)
        Assert.assertEquals("rookie", rookieInfo.typeKey)
    }

    // endregion

    // region Type Per File

    @Test
    fun testTypePerFile() {
        val file = configureScriptFile(
            "common/space_stations/orbital_alpha.txt",
            "features/resolve/common/space_stations/orbital_alpha.txt"
        )

        // space_station 类型使用 type_per_file = yes，整个文件是一个定义
        val info = ParadoxDefinitionManager.getInfo(file)
        Assert.assertNotNull(info)
        info!!
        Assert.assertEquals("orbital_alpha", info.name)
        Assert.assertEquals("space_station", info.type)
        Assert.assertEquals("orbital_alpha", info.typeKey)
        Assert.assertEquals(ParadoxDefinitionSource.File, info.source)
    }

    // endregion

    // region Member Path

    @Test
    fun testMemberPath_BasicProperty() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!
        Assert.assertEquals("titan_mk3", titanInfo.memberPath.path)
    }

    @Test
    fun testMemberPath_TypePerFile() {
        val file = configureScriptFile(
            "common/space_stations/orbital_alpha.txt",
            "features/resolve/common/space_stations/orbital_alpha.txt"
        )

        val info = ParadoxDefinitionManager.getInfo(file)!!
        // 文件级定义的成员路径为空
        Assert.assertTrue(info.memberPath.isEmpty())
    }

    // endregion

    // region Declaration

    @Test
    fun testDeclaration_BasicStructure() {
        val file = configureScriptFile("common/signals/00_signals.txt", "features/resolve/common/signals/00_signals.txt")

        val distress = selectScope { file.ofPath("distress_beacon").asProperty().one() }!!
        val distressInfo = ParadoxDefinitionManager.getInfo(distress)!!

        val declaration = distressInfo.declaration
        Assert.assertNotNull(declaration)
        declaration!!

        // 使用 getIdentifierKey 验证声明结构
        val key = CwtConfigManipulator.getIdentifierKey(declaration, "\u0000", -1)
        // signal 声明应包含 frequency 和 priority 字段
        Assert.assertTrue(key.contains("frequency"))
        Assert.assertTrue(key.contains("priority"))
    }

    @Test
    fun testDeclaration_WithHeavySubtype() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // titan_mk3 有子类型 "heavy"
        val titan = selectScope { file.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!

        val declaration = titanInfo.declaration
        Assert.assertNotNull(declaration)
        declaration!!

        val key = CwtConfigManipulator.getIdentifierKey(declaration, "\u0000", -1)
        // 基础字段
        Assert.assertTrue(key.contains("armor"))
        Assert.assertTrue(key.contains("speed"))
        // 子类型 heavy 的字段应被打平到声明中
        Assert.assertTrue(key.contains("shield"))
        Assert.assertTrue(key.contains("weight_class"))
        // 子类型 stealth 的字段不应出现
        Assert.assertFalse(key.contains("cloaking"))
    }

    @Test
    fun testDeclaration_WithStealthSubtype() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // phantom 有子类型 "stealth"
        val phantom = selectScope { file.ofPath("phantom").asProperty().one() }!!
        val phantomInfo = ParadoxDefinitionManager.getInfo(phantom)!!

        val declaration = phantomInfo.declaration
        Assert.assertNotNull(declaration)
        declaration!!

        val key = CwtConfigManipulator.getIdentifierKey(declaration, "\u0000", -1)
        // 基础字段
        Assert.assertTrue(key.contains("armor"))
        Assert.assertTrue(key.contains("speed"))
        // 子类型 stealth 的字段应被打平到声明中
        Assert.assertTrue(key.contains("cloaking"))
        // 子类型 heavy 的字段不应出现
        Assert.assertFalse(key.contains("shield"))
        Assert.assertFalse(key.contains("weight_class"))
    }

    @Test
    fun testDeclaration_NoSubtype() {
        val file = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")

        // vanguard 没有子类型
        val vanguard = selectScope { file.ofPath("vanguard").asProperty().one() }!!
        val vanguardInfo = ParadoxDefinitionManager.getInfo(vanguard)!!

        val declaration = vanguardInfo.declaration
        Assert.assertNotNull(declaration)
        declaration!!

        val key = CwtConfigManipulator.getIdentifierKey(declaration, "\u0000", -1)
        // 只有基础字段
        Assert.assertTrue(key.contains("armor"))
        Assert.assertTrue(key.contains("speed"))
        // 任何子类型字段都不应出现
        Assert.assertFalse(key.contains("shield"))
        Assert.assertFalse(key.contains("weight_class"))
        Assert.assertFalse(key.contains("cloaking"))
    }

    @Test
    fun testDeclaration_DifferentTypes_DifferentStructure() {
        val mechFile = configureScriptFile("common/mechs/00_mechs.txt", "features/resolve/common/mechs/00_mechs.txt")
        val weaponFile = configureScriptFile("common/weapons/00_weapons.txt", "features/resolve/common/weapons/00_weapons.txt")

        val titan = selectScope { mechFile.ofPath("titan_mk3").asProperty().one() }!!
        val titanInfo = ParadoxDefinitionManager.getInfo(titan)!!
        val titanKey = CwtConfigManipulator.getIdentifierKey(titanInfo.declaration!!, "\u0000", -1)

        val plasma = selectScope { weaponFile.ofPath("plasma_cannon").asProperty().one() }!!
        val plasmaInfo = ParadoxDefinitionManager.getInfo(plasma)!!
        val plasmaKey = CwtConfigManipulator.getIdentifierKey(plasmaInfo.declaration!!, "\u0000", -1)

        // 不同类型的声明结构应不同
        Assert.assertNotEquals(titanKey, plasmaKey)
        // mech 声明包含 armor，weapon 声明包含 damage
        Assert.assertTrue(titanKey.contains("armor"))
        Assert.assertTrue(plasmaKey.contains("damage"))
        Assert.assertFalse(titanKey.contains("damage"))
        Assert.assertFalse(plasmaKey.contains("armor"))
    }

    // endregion
}

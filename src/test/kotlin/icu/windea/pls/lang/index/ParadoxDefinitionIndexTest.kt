package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxGameType
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
class ParadoxDefinitionIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun clear() = clearIntegrationTest()

    // region Basic Property Definition

    @Test
    fun testDefinitionIndex_BasicProperty() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/starships/00_starships.txt")
        val psiFile = myFixture.configureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: __all__ 包含 3 个定义
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(3, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "starship" })
        Assert.assertTrue(allInfos.all { it.source == ParadoxDefinitionSource.Property })
        Assert.assertTrue(allInfos.all { it.gameType == ParadoxGameType.Stellaris })

        // Assert: type key
        val typeInfos = fileData[PlsIndexUtil.createTypeKey("starship")].orEmpty()
        Assert.assertEquals(3, typeInfos.size)

        // Assert: name keys
        val expectedNames = setOf("explorer", "battlecruiser", "interceptor")
        Assert.assertEquals(expectedNames, allInfos.map { it.name }.toSet())
        allInfos.forEach { info ->
            // typeKey 与 name 一致（基础场景）
            Assert.assertEquals(info.name, info.typeKey)
            // name key 存在
            Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey(info.name)])
            Assert.assertNotNull(fileData[PlsIndexUtil.createNameTypeKey(info.name, "starship")])
        }
    }

    @Test
    fun testDefinitionIndex_BasicProperty_ElementOffset() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/starships/00_starships.txt")
        val psiFile = myFixture.configureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 每个定义的 elementOffset 互不相同
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        val offsets = allInfos.map { it.elementOffset }.toSet()
        Assert.assertEquals(3, offsets.size)
        Assert.assertTrue(offsets.all { it >= 0 })
    }

    // endregion

    // region Type Per File

    @Test
    fun testDefinitionIndex_TypePerFile() {
        // Arrange & Act: ocean_world
        markFileInfo(ParadoxGameType.Stellaris, "common/planet_classes/ocean_world.txt")
        val oceanFile = myFixture.configureByFile("features/index/common/planet_classes/ocean_world.txt")

        val project = project
        val oceanData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, oceanFile.virtualFile, project)

        // Assert: 文件级定义
        val oceanAll = oceanData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(1, oceanAll.size)
        val oceanInfo = oceanAll.single()
        Assert.assertEquals("ocean_world", oceanInfo.name)
        Assert.assertEquals("planet_class", oceanInfo.type)
        Assert.assertEquals("ocean_world", oceanInfo.typeKey)
        Assert.assertEquals(ParadoxDefinitionSource.File, oceanInfo.source)
        Assert.assertEquals(ParadoxGameType.Stellaris, oceanInfo.gameType)

        // Assert: name key 和 type key
        Assert.assertNotNull(oceanData[PlsIndexUtil.createNameKey("ocean_world")])
        Assert.assertNotNull(oceanData[PlsIndexUtil.createTypeKey("planet_class")])
        Assert.assertNotNull(oceanData[PlsIndexUtil.createNameTypeKey("ocean_world", "planet_class")])
    }

    @Test
    fun testDefinitionIndex_TypePerFile_MultipleFiles() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/planet_classes/ocean_world.txt")
        myFixture.configureByFile("features/index/common/planet_classes/ocean_world.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/planet_classes/desert_world.txt")
        myFixture.configureByFile("features/index/common/planet_classes/desert_world.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val typeKey = PlsIndexUtil.createTypeKey("planet_class")
        val allPlanetInfos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.Definition, typeKey, scope).flatten()

        // Assert
        Assert.assertEquals(2, allPlanetInfos.size)
        Assert.assertEquals(setOf("ocean_world", "desert_world"), allPlanetInfos.map { it.name }.toSet())
        Assert.assertTrue(allPlanetInfos.all { it.source == ParadoxDefinitionSource.File })
    }

    // endregion

    // region Name Field

    @Test
    fun testDefinitionIndex_NameField() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/alien_species/00_species.txt")
        val psiFile = myFixture.configureByFile("features/index/common/alien_species/00_species.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 2 个定义，name 来自 species_name 字段
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(2, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "alien_species" })

        val expectedNames = setOf("zephyr_folk", "crystal_entity")
        Assert.assertEquals(expectedNames, allInfos.map { it.name }.toSet())

        // typeKey 仍然是属性键，但 name 是 species_name 的值
        val zephyrInfo = allInfos.single { it.name == "zephyr_folk" }
        Assert.assertEquals("zephyrian", zephyrInfo.typeKey)
        val crystalInfo = allInfos.single { it.name == "crystal_entity" }
        Assert.assertEquals("crystalborn", crystalInfo.typeKey)

        // name key 应基于 species_name 的值
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey("zephyr_folk")])
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey("crystal_entity")])
        // 属性键不应作为 name key
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("zephyrian")])
    }

    // endregion

    // region Anonymous (name_field = "")

    @Test
    fun testDefinitionIndex_AnonymousNameField() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/star_systems/00_systems.txt")
        val psiFile = myFixture.configureByFile("features/index/common/star_systems/00_systems.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 匿名定义仍出现在 __all__ 和 type key 下
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(2, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "star_system" })
        Assert.assertTrue(allInfos.all { it.name.isEmpty() })

        val typeInfos = fileData[PlsIndexUtil.createTypeKey("star_system")].orEmpty()
        Assert.assertEquals(2, typeInfos.size)

        // 匿名定义不生成 name key
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("sol")])
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("alpha_centauri")])
    }

    // endregion

    // region Skip Root Key

    @Test
    fun testDefinitionIndex_SkipRootKey() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/space_stations/00_stations.txt")
        val psiFile = myFixture.configureByFile("features/index/common/space_stations/00_stations.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 跳过 "stations" 键，索引其下的子属性
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(2, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "space_station" })

        val expectedNames = setOf("orbital_hub", "defense_platform")
        Assert.assertEquals(expectedNames, allInfos.map { it.name }.toSet())

        // "stations" 本身不应被索引为定义
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("stations")])
        // 子属性应被索引
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey("orbital_hub")])
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey("defense_platform")])
    }

    // endregion

    // region Type Key Filter

    @Test
    fun testDefinitionIndex_TypeKeyFilter_Inclusion() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/drives/00_drives.txt")
        val psiFile = myFixture.configureByFile("features/index/common/drives/00_drives.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: warp_drive 和 hyperdrive 匹配 ftl_drive（包含过滤）
        val ftlInfos = fileData[PlsIndexUtil.createTypeKey("ftl_drive")].orEmpty()
        Assert.assertEquals(2, ftlInfos.size)
        Assert.assertEquals(setOf("warp_drive", "hyperdrive"), ftlInfos.map { it.name }.toSet())
    }

    @Test
    fun testDefinitionIndex_TypeKeyFilter_Exclusion() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/drives/00_drives.txt")
        val psiFile = myFixture.configureByFile("features/index/common/drives/00_drives.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: ion_thruster 匹配 sublight_drive（排除过滤）
        val sublightInfos = fileData[PlsIndexUtil.createTypeKey("sublight_drive")].orEmpty()
        Assert.assertEquals(1, sublightInfos.size)
        Assert.assertEquals("ion_thruster", sublightInfos.single().name)
    }

    @Test
    fun testDefinitionIndex_TypeKeyFilter_AllDefinitionsPresent() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/drives/00_drives.txt")
        val psiFile = myFixture.configureByFile("features/index/common/drives/00_drives.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 共 3 个定义（2 ftl_drive + 1 sublight_drive）
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(3, allInfos.size)
        Assert.assertEquals(setOf("ftl_drive", "sublight_drive"), allInfos.map { it.type }.toSet())
    }

    // endregion

    // region Starts With

    @Test
    fun testDefinitionIndex_StartsWith() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/districts/00_districts.txt")
        val psiFile = myFixture.configureByFile("features/index/common/districts/00_districts.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 3 个 district 定义
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(3, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "district" })

        // name 应去除 "d_" 前缀
        val expectedNames = setOf("city", "mining", "generator")
        Assert.assertEquals(expectedNames, allInfos.map { it.name }.toSet())

        // typeKey 保留完整前缀
        val expectedTypeKeys = setOf("d_city", "d_mining", "d_generator")
        Assert.assertEquals(expectedTypeKeys, allInfos.map { it.typeKey }.toSet())

        // name key 基于去除前缀后的名称
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey("city")])
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameTypeKey("city", "district")])
        // 完整 typeKey 不应作为 name key
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("d_city")])
    }

    @Test
    fun testDefinitionIndex_StartsWith_PrefixMismatch() {
        // Arrange: districts 路径但包含不以 "d_" 开头的属性键
        // 由于 starts_with 要求 typeKey 匹配该前缀，不匹配前缀的属性不应被索引
        markFileInfo(ParadoxGameType.Stellaris, "common/districts/00_districts.txt")
        val psiFile = myFixture.configureByFile("features/index/common/districts/00_districts.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 所有 typeKey 都以 "d_" 开头
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertTrue(allInfos.all { it.typeKey.startsWith("d_") })
    }

    // endregion

    // region Type Key Regex

    @Test
    fun testDefinitionIndex_TypeKeyRegex() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/fleets/00_fleets.txt")
        val psiFile = myFixture.configureByFile("features/index/common/fleets/00_fleets.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 仅匹配 ^fleet_.* 正则的属性被索引为 fleet_template
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(2, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "fleet_template" })
        Assert.assertEquals(setOf("fleet_assault", "fleet_patrol"), allInfos.map { it.name }.toSet())

        // solo_corvette 不匹配正则，不应被索引
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("solo_corvette")])
    }

    // endregion

    // region Skip Root Key Alternatives

    @Test
    fun testDefinitionIndex_SkipRootKey_Alternatives() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/garrisons/00_garrisons.txt")
        val psiFile = myFixture.configureByFile("features/index/common/garrisons/00_garrisons.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 跳过 ground_forces 和 naval_forces 两个根键，索引其下所有子定义
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(3, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "garrison" })

        val expectedNames = setOf("militia", "elite_guard", "coastal_battery")
        Assert.assertEquals(expectedNames, allInfos.map { it.name }.toSet())

        // 根键本身不应被索引
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("ground_forces")])
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("naval_forces")])
    }

    // endregion

    // region Name Field Dash (name from property value)

    @Test
    fun testDefinitionIndex_NameFieldDash() {
        // Arrange
        markFileInfo(ParadoxGameType.Stellaris, "common/anomalies/00_anomalies.txt")
        val psiFile = myFixture.configureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 名称取自属性值（而非属性键）
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(3, allInfos.size)
        Assert.assertTrue(allInfos.all { it.type == "anomaly" })

        val expectedNames = setOf("alien_signal", "debris_field", "ancient_ruin")
        Assert.assertEquals(expectedNames, allInfos.map { it.name }.toSet())

        // typeKey 仍是属性键
        val expectedTypeKeys = setOf("anomaly_1", "anomaly_2", "anomaly_3")
        Assert.assertEquals(expectedTypeKeys, allInfos.map { it.typeKey }.toSet())

        // name key 基于属性值
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameKey("alien_signal")])
        Assert.assertNotNull(fileData[PlsIndexUtil.createNameTypeKey("debris_field", "anomaly")])
        // 属性键不应作为 name key
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("anomaly_1")])
    }

    // endregion

    // region Cross-File Aggregation

    @Test
    fun testDefinitionIndex_CrossFileAggregation_ByType() {
        // Arrange: 多个文件的同类型定义
        markFileInfo(ParadoxGameType.Stellaris, "common/planet_classes/ocean_world.txt")
        myFixture.configureByFile("features/index/common/planet_classes/ocean_world.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/planet_classes/desert_world.txt")
        myFixture.configureByFile("features/index/common/planet_classes/desert_world.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val allKey = PlsIndexUtil.createAllKey()
        val allInfos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.Definition, allKey, scope).flatten()

        // Assert: 跨文件聚合
        val planetInfos = allInfos.filter { it.type == "planet_class" }
        Assert.assertEquals(2, planetInfos.size)
        Assert.assertEquals(setOf("ocean_world", "desert_world"), planetInfos.map { it.name }.toSet())
    }

    @Test
    fun testDefinitionIndex_CrossFileAggregation_ByName() {
        // Arrange: 跨文件按 name key 聚合
        markFileInfo(ParadoxGameType.Stellaris, "common/starships/00_starships.txt")
        myFixture.configureByFile("features/index/common/starships/00_starships.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/drives/00_drives.txt")
        myFixture.configureByFile("features/index/common/drives/00_drives.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val nameKey = PlsIndexUtil.createNameKey("explorer")
        val infos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.Definition, nameKey, scope).flatten()

        // Assert: 仅有 starship 类型的 explorer
        Assert.assertEquals(1, infos.size)
        Assert.assertEquals("explorer", infos.single().name)
        Assert.assertEquals("starship", infos.single().type)
    }

    @Test
    fun testDefinitionIndex_CrossFileAggregation_MultipleTypes() {
        // Arrange: 加载不同类型的多个文件
        markFileInfo(ParadoxGameType.Stellaris, "common/starships/00_starships.txt")
        myFixture.configureByFile("features/index/common/starships/00_starships.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/districts/00_districts.txt")
        myFixture.configureByFile("features/index/common/districts/00_districts.txt")
        markFileInfo(ParadoxGameType.Stellaris, "common/anomalies/00_anomalies.txt")
        myFixture.configureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val allKey = PlsIndexUtil.createAllKey()
        val allInfos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.Definition, allKey, scope).flatten()

        // Assert: 3 starship + 3 district + 3 anomaly = 9
        Assert.assertEquals(9, allInfos.size)
        Assert.assertEquals(setOf("starship", "district", "anomaly"), allInfos.map { it.type }.toSet())
    }

    // endregion

    // region No Matched Type

    @Test
    fun testDefinitionIndex_NoMatchedType() {
        // Arrange: 使用一个没有匹配类型规则的路径
        markFileInfo(ParadoxGameType.Stellaris, "common/no_rule/00_data.txt")
        val psiFile = myFixture.configureByFile("features/index/common/no_rule/01_inject.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.Definition, psiFile.virtualFile, project)

        // Assert: 无索引数据
        Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
    }

    // endregion
}

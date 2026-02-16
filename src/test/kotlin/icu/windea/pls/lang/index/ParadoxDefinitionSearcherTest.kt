package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
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
class ParadoxDefinitionSearcherTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun clear() = clearIntegrationTest()

    private fun configureScriptFile(relPath: String, @TestDataFile testDataPath: String) {
        markFileInfo(gameType, relPath)
        myFixture.configureByFile(testDataPath)
    }

    // region Search All

    @Test
    fun testDefinitionSearch_All() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        configureScriptFile("common/drives/00_drives.txt", "features/index/common/drives/00_drives.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: starship(3) + ftl_drive(2) + sublight_drive(1) = 6
        Assert.assertEquals(6, results.size)
        Assert.assertTrue(results.any { it.name == "explorer" && it.type == "starship" })
        Assert.assertTrue(results.any { it.name == "battlecruiser" && it.type == "starship" })
        Assert.assertTrue(results.any { it.name == "interceptor" && it.type == "starship" })
        Assert.assertTrue(results.any { it.name == "warp_drive" && it.type == "ftl_drive" })
        Assert.assertTrue(results.any { it.name == "hyperdrive" && it.type == "ftl_drive" })
        Assert.assertTrue(results.any { it.name == "ion_thruster" && it.type == "sublight_drive" })
    }

    // endregion

    // region Search By Name

    @Test
    fun testDefinitionSearch_ByName() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionSearch.search("explorer", null, selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("explorer", result.name)
        Assert.assertEquals("starship", result.type)
        Assert.assertEquals(ParadoxDefinitionSource.Property, result.source)
    }

    @Test
    fun testDefinitionSearch_ByName_NotFound() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionSearch.search("nonexistent_ship", null, selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search By Type

    @Test
    fun testDefinitionSearch_ByType() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        configureScriptFile("common/drives/00_drives.txt", "features/index/common/drives/00_drives.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, "starship", selector).findAll()

        // Assert
        Assert.assertEquals(3, results.size)
        Assert.assertTrue(results.all { it.type == "starship" })
        Assert.assertEquals(setOf("explorer", "battlecruiser", "interceptor"), results.map { it.name }.toSet())
    }

    @Test
    fun testDefinitionSearch_ByType_FtlDrive() {
        // Arrange
        configureScriptFile("common/drives/00_drives.txt", "features/index/common/drives/00_drives.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, "ftl_drive", selector).findAll()

        // Assert: 仅 warp_drive 和 hyperdrive 匹配 ftl_drive
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("warp_drive", "hyperdrive"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search By Name And Type

    @Test
    fun testDefinitionSearch_ByNameAndType() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionSearch.search("battlecruiser", "starship", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("battlecruiser", result.name)
        Assert.assertEquals("starship", result.type)
    }

    @Test
    fun testDefinitionSearch_ByNameAndType_Mismatch() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 名字存在但类型不匹配
        val result = ParadoxDefinitionSearch.search("explorer", "ftl_drive", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search Element

    @Test
    fun testDefinitionSearch_SearchElement() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val elements = ParadoxDefinitionSearch.searchElement("explorer", "starship", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("explorer", elements.single().name)
    }

    @Test
    fun testDefinitionSearch_SearchElement_TypePerFile() {
        // Arrange
        configureScriptFile("common/planet_classes/ocean_world.txt", "features/index/common/planet_classes/ocean_world.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val elements = ParadoxDefinitionSearch.searchElement("ocean_world", "planet_class", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        // 文件级定义的元素应为 ParadoxScriptFile
        Assert.assertTrue(elements.single() is icu.windea.pls.script.psi.ParadoxScriptFile)
    }

    @Test
    fun testDefinitionSearch_SearchElement_NameField() {
        // Arrange
        configureScriptFile("common/alien_species/00_species.txt", "features/index/common/alien_species/00_species.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用 name_field 的值搜索
        val elements = ParadoxDefinitionSearch.searchElement("zephyr_folk", "alien_species", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        // 对应的 PSI 属性键是 "zephyrian"
        Assert.assertEquals("zephyrian", elements.single().name)
    }

    // endregion

    // region Search with File Scope

    @Test
    fun testDefinitionSearch_WithFileScope() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        val starshipsFile = myFixture.file.virtualFile
        configureScriptFile("common/drives/00_drives.txt", "features/index/common/drives/00_drives.txt")

        val fileScope = GlobalSearchScope.fileScope(project, starshipsFile)
        val selector = selector(project, myFixture.file).definition().withSearchScope(fileScope)

        // Act: 仅搜索 starships 文件
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 仅包含 starship 定义
        Assert.assertEquals(3, results.size)
        Assert.assertTrue(results.all { it.type == "starship" })
    }

    // endregion

    // region Search Skip Root Key

    @Test
    fun testDefinitionSearch_SkipRootKey() {
        // Arrange
        configureScriptFile("common/space_stations/00_stations.txt", "features/index/common/space_stations/00_stations.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, "space_station", selector).findAll()

        // Assert: 跳过 "stations" 顶级键，索引其下的子定义
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("orbital_hub", "defense_platform"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search Anonymous

    @Test
    fun testDefinitionSearch_Anonymous() {
        // Arrange
        configureScriptFile("common/star_systems/00_systems.txt", "features/index/common/star_systems/00_systems.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 按类型搜索匿名定义
        val results = ParadoxDefinitionSearch.search(null, "star_system", selector).findAll()

        // Assert: 匿名定义也能通过类型查找
        Assert.assertEquals(2, results.size)
        Assert.assertTrue(results.all { it.name.isEmpty() })
        Assert.assertTrue(results.all { it.type == "star_system" })
    }

    @Test
    fun testDefinitionSearch_Anonymous_ByNameReturnsNothing() {
        // Arrange
        configureScriptFile("common/star_systems/00_systems.txt", "features/index/common/star_systems/00_systems.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用属性键名搜索不应返回匿名定义
        val result = ParadoxDefinitionSearch.search("sol", "star_system", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search Starts With

    @Test
    fun testDefinitionSearch_StartsWith() {
        // Arrange
        configureScriptFile("common/districts/00_districts.txt", "features/index/common/districts/00_districts.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用去除前缀后的名称搜索
        val result = ParadoxDefinitionSearch.search("city", "district", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("city", result.name)
        Assert.assertEquals("district", result.type)
        Assert.assertEquals("d_city", result.typeKey)
    }

    @Test
    fun testDefinitionSearch_StartsWith_AllByType() {
        // Arrange
        configureScriptFile("common/districts/00_districts.txt", "features/index/common/districts/00_districts.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, "district", selector).findAll()

        // Assert
        Assert.assertEquals(3, results.size)
        Assert.assertEquals(setOf("city", "mining", "generator"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search Type Key Regex

    @Test
    fun testDefinitionSearch_TypeKeyRegex() {
        // Arrange
        configureScriptFile("common/fleets/00_fleets.txt", "features/index/common/fleets/00_fleets.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, "fleet_template", selector).findAll()

        // Assert: 仅匹配正则的定义
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("fleet_assault", "fleet_patrol"), results.map { it.name }.toSet())
    }

    @Test
    fun testDefinitionSearch_TypeKeyRegex_ByName() {
        // Arrange
        configureScriptFile("common/fleets/00_fleets.txt", "features/index/common/fleets/00_fleets.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 不匹配正则的属性名搜索不到结果
        val result = ParadoxDefinitionSearch.search("solo_corvette", "fleet_template", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search Skip Root Key Alternatives

    @Test
    fun testDefinitionSearch_SkipRootKey_Alternatives() {
        // Arrange
        configureScriptFile("common/garrisons/00_garrisons.txt", "features/index/common/garrisons/00_garrisons.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, "garrison", selector).findAll()

        // Assert: 跨两种根键，所有子定义均可搜到
        Assert.assertEquals(3, results.size)
        Assert.assertEquals(setOf("militia", "elite_guard", "coastal_battery"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search Name Field Dash

    @Test
    fun testDefinitionSearch_NameFieldDash() {
        // Arrange
        configureScriptFile("common/anomalies/00_anomalies.txt", "features/index/common/anomalies/00_anomalies.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用属性值（name_field = "-"）搜索
        val result = ParadoxDefinitionSearch.search("alien_signal", "anomaly", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("alien_signal", result.name)
        Assert.assertEquals("anomaly_1", result.typeKey)
    }

    @Test
    fun testDefinitionSearch_NameFieldDash_TypeKeyNotSearchable() {
        // Arrange
        configureScriptFile("common/anomalies/00_anomalies.txt", "features/index/common/anomalies/00_anomalies.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用属性键搜索应返回空（因为 name 取自属性值）
        val result = ParadoxDefinitionSearch.search("anomaly_1", "anomaly", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region searchFile / searchProperty

    @Test
    fun testDefinitionSearch_SearchFile_TypePerFile() {
        // Arrange
        configureScriptFile("common/planet_classes/ocean_world.txt", "features/index/common/planet_classes/ocean_world.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val files = ParadoxDefinitionSearch.searchFile("ocean_world", "planet_class", selector).findAll()

        // Assert: 文件级定义通过 searchFile 可获取 PsiFile
        Assert.assertEquals(1, files.size)
        Assert.assertTrue(files.single() is icu.windea.pls.script.psi.ParadoxScriptFile)
    }

    @Test
    fun testDefinitionSearch_SearchFile_PropertyDefinition() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 属性级定义通过 searchFile 应返回空
        val files = ParadoxDefinitionSearch.searchFile("explorer", "starship", selector).findAll()

        // Assert
        Assert.assertTrue(files.isEmpty())
    }

    @Test
    fun testDefinitionSearch_SearchProperty() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val properties = ParadoxDefinitionSearch.searchProperty("explorer", "starship", selector).findAll()

        // Assert: 属性级定义通过 searchProperty 可获取 ParadoxScriptProperty
        Assert.assertEquals(1, properties.size)
        Assert.assertEquals("explorer", properties.single().name)
    }

    @Test
    fun testDefinitionSearch_SearchProperty_FileDefinition() {
        // Arrange
        configureScriptFile("common/planet_classes/ocean_world.txt", "features/index/common/planet_classes/ocean_world.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 文件级定义通过 searchProperty 应返回空
        val properties = ParadoxDefinitionSearch.searchProperty("ocean_world", "planet_class", selector).findAll()

        // Assert
        Assert.assertTrue(properties.isEmpty())
    }

    // endregion

    // region Comprehensive Cross-Type Search

    @Test
    fun testDefinitionSearch_Comprehensive_AllTypes() {
        // Arrange: 加载所有类型的测试文件
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        configureScriptFile("common/planet_classes/ocean_world.txt", "features/index/common/planet_classes/ocean_world.txt")
        configureScriptFile("common/planet_classes/desert_world.txt", "features/index/common/planet_classes/desert_world.txt")
        configureScriptFile("common/alien_species/00_species.txt", "features/index/common/alien_species/00_species.txt")
        configureScriptFile("common/star_systems/00_systems.txt", "features/index/common/star_systems/00_systems.txt")
        configureScriptFile("common/space_stations/00_stations.txt", "features/index/common/space_stations/00_stations.txt")
        configureScriptFile("common/drives/00_drives.txt", "features/index/common/drives/00_drives.txt")
        configureScriptFile("common/districts/00_districts.txt", "features/index/common/districts/00_districts.txt")
        configureScriptFile("common/fleets/00_fleets.txt", "features/index/common/fleets/00_fleets.txt")
        configureScriptFile("common/garrisons/00_garrisons.txt", "features/index/common/garrisons/00_garrisons.txt")
        configureScriptFile("common/anomalies/00_anomalies.txt", "features/index/common/anomalies/00_anomalies.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 各类型定义总数
        // starship(3) + planet_class(2) + alien_species(2) + star_system(2)
        // + space_station(2) + ftl_drive(2) + sublight_drive(1)
        // + district(3) + fleet_template(2) + garrison(3) + anomaly(3)
        Assert.assertEquals(25, results.size)

        // 验证每种类型的数量
        val byType = results.groupBy { it.type }
        Assert.assertEquals(3, byType["starship"]?.size)
        Assert.assertEquals(2, byType["planet_class"]?.size)
        Assert.assertEquals(2, byType["alien_species"]?.size)
        Assert.assertEquals(2, byType["star_system"]?.size)
        Assert.assertEquals(2, byType["space_station"]?.size)
        Assert.assertEquals(2, byType["ftl_drive"]?.size)
        Assert.assertEquals(1, byType["sublight_drive"]?.size)
        Assert.assertEquals(3, byType["district"]?.size)
        Assert.assertEquals(2, byType["fleet_template"]?.size)
        Assert.assertEquals(3, byType["garrison"]?.size)
        Assert.assertEquals(3, byType["anomaly"]?.size)
    }

    @Test
    fun testDefinitionSearch_Comprehensive_MixedSourceTypes() {
        // Arrange: 加载文件级和属性级定义
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        configureScriptFile("common/planet_classes/ocean_world.txt", "features/index/common/planet_classes/ocean_world.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 混合 source 类型
        Assert.assertEquals(4, results.size)
        val fileSources = results.filter { it.source == ParadoxDefinitionSource.File }
        val propertySources = results.filter { it.source == ParadoxDefinitionSource.Property }
        Assert.assertEquals(1, fileSources.size)
        Assert.assertEquals("planet_class", fileSources.single().type)
        Assert.assertEquals(3, propertySources.size)
        Assert.assertTrue(propertySources.all { it.type == "starship" })
    }

    @Test
    fun testDefinitionSearch_Comprehensive_MultiFileScope() {
        // Arrange
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        val starshipsFile = myFixture.file.virtualFile
        configureScriptFile("common/districts/00_districts.txt", "features/index/common/districts/00_districts.txt")
        val districtsFile = myFixture.file.virtualFile
        configureScriptFile("common/anomalies/00_anomalies.txt", "features/index/common/anomalies/00_anomalies.txt")

        // Act: 仅搜索 starships + districts 两个文件
        val unionScope = GlobalSearchScope.filesScope(project, listOf(starshipsFile, districtsFile))
        val selector = selector(project, myFixture.file).definition().withSearchScope(unionScope)
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 3 starship + 3 district = 6（排除 anomaly）
        Assert.assertEquals(6, results.size)
        Assert.assertEquals(setOf("starship", "district"), results.map { it.type }.toSet())
    }

    // endregion

    // region Edge Cases

    @Test
    fun testDefinitionSearch_EmptyFile() {
        // Arrange: 文件仅含注释
        configureScriptFile("common/starships/02_empty.txt", "features/index/common/starships/02_empty.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert
        Assert.assertTrue(results.isEmpty())
    }

    @Test
    fun testDefinitionSearch_SearchElement_StartsWith() {
        // Arrange
        configureScriptFile("common/districts/00_districts.txt", "features/index/common/districts/00_districts.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用去前缀的 name 搜索 element
        val elements = ParadoxDefinitionSearch.searchElement("city", "district", selector).findAll()

        // Assert: PSI 元素的属性键保留完整前缀
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("d_city", elements.single().name)
    }

    @Test
    fun testDefinitionSearch_SearchElement_NameFieldDash() {
        // Arrange
        configureScriptFile("common/anomalies/00_anomalies.txt", "features/index/common/anomalies/00_anomalies.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 用属性值（name_field="-"的 name）搜索 element
        val elements = ParadoxDefinitionSearch.searchElement("alien_signal", "anomaly", selector).findAll()

        // Assert: PSI 元素的属性键是 typeKey（anomaly_1），而非 name
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("anomaly_1", elements.single().name)
    }

    @Test
    fun testDefinitionSearch_ByNameOnly_AcrossMultipleTypes() {
        // Arrange: 加载多种类型的文件
        configureScriptFile("common/starships/00_starships.txt", "features/index/common/starships/00_starships.txt")
        configureScriptFile("common/drives/00_drives.txt", "features/index/common/drives/00_drives.txt")
        configureScriptFile("common/districts/00_districts.txt", "features/index/common/districts/00_districts.txt")

        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 仅按名称搜索，不指定类型
        val result = ParadoxDefinitionSearch.search("warp_drive", null, selector).findFirst()

        // Assert: 能找到且类型正确
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("warp_drive", result.name)
        Assert.assertEquals("ftl_drive", result.type)
    }

    // endregion

    // region From Injection (create_mode)

    @Test
    fun testDefinitionIndex_DefinitionInjection_ReplaceOrCreate() {
        // Arrange: REPLACE_OR_CREATE 模式的定义注入应被索引为定义
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")

        // Act
        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 应有 1 个来自 REPLACE_OR_CREATE 的定义
        val injectionInfos = results.filter { it.source == ParadoxDefinitionSource.Injection }
        Assert.assertEquals(1, injectionInfos.size)

        val injectionInfo = injectionInfos.single()
        Assert.assertEquals("tome_of_new", injectionInfo.name)
        Assert.assertEquals("arcane_tome", injectionInfo.type)
        Assert.assertEquals("tome_of_new", injectionInfo.typeKey)
        Assert.assertEquals(ParadoxDefinitionSource.Injection, injectionInfo.source)
        Assert.assertEquals(gameType, injectionInfo.gameType)
    }

    @Test
    fun testDefinitionIndex_DefinitionInjection_NonDefinitionModes_NotIndexed() {
        // Arrange: INJECT/REPLACE/TRY_INJECT 等非 create_mode 不应被索引为定义
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")

        // Act
        val selector = selector(project, myFixture.file).definition().withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: INJECT:tome_of_flames 不应被索引
        val tomeOfFlamesInfos = results.filter { it.name == "tome_of_flames" }
        Assert.assertTrue("INJECT mode should not be indexed as definition", tomeOfFlamesInfos.isEmpty())

        // Assert: REPLACE:tome_of_ice 不应被索引
        val tomeOfIceInfos = results.filter { it.name == "tome_of_ice" }
        Assert.assertTrue("REPLACE mode should not be indexed as definition", tomeOfIceInfos.isEmpty())

        // Assert: TRY_INJECT:shared_name 不应被索引
        val sharedNameInfos = results.filter { it.name == "shared_name" }
        Assert.assertTrue("TRY_INJECT mode should not be indexed as definition", sharedNameInfos.isEmpty())
    }

    // endregion
}

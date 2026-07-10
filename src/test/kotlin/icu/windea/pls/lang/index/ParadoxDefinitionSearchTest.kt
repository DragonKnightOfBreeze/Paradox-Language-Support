package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.withSearchScope
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

/**
 * @see ParadoxDefinitionSearch
 * @see icu.windea.pls.lang.search.searchers.ParadoxDefinitionSearcher
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDefinitionSearchTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun markAndConfigureByFile(@TestDataFile testDataPath: String, relPath: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, relPath)
        return myFixture.configureByFile(testDataPath)
    }

    // region Search All

    @Test
    fun test_All() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        markAndConfigureByFile("features/index/common/drives/00_drives.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
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
    fun test_ByName() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("explorer", null, selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("explorer", result.name)
        Assert.assertEquals("starship", result.type)
        Assert.assertEquals(ParadoxDefinitionSource.Property, result.source)
    }

    @Test
    fun test_ByName_NotFound() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("nonexistent_ship", null, selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search By Type

    @Test
    fun test_ByType() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        markAndConfigureByFile("features/index/common/drives/00_drives.txt")


        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "starship", selector).findAll()

        // Assert
        Assert.assertEquals(3, results.size)
        Assert.assertTrue(results.all { it.type == "starship" })
        Assert.assertEquals(setOf("explorer", "battlecruiser", "interceptor"), results.map { it.name }.toSet())
    }

    @Test
    fun test_ByType_FtlDrive() {
        // Arrange
        markAndConfigureByFile("features/index/common/drives/00_drives.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "ftl_drive", selector).findAll()

        // Assert: 仅 warp_drive 和 hyperdrive 匹配 ftl_drive
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("warp_drive", "hyperdrive"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search By Name And Type

    @Test
    fun test_ByNameAndType() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("battlecruiser", "starship", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("battlecruiser", result.name)
        Assert.assertEquals("starship", result.type)
    }

    @Test
    fun test_ByNameAndType_Mismatch() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act: 名字存在但类型不匹配
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("explorer", "ftl_drive", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search Element

    @Test
    fun test_SearchElement() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val elements = ParadoxDefinitionSearch.searchElement("explorer", "starship", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("explorer", elements.single().name)
    }

    @Test
    fun test_SearchElement_TypePerFile() {
        // Arrange
        markAndConfigureByFile("features/index/common/planet_classes/ocean_world.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val elements = ParadoxDefinitionSearch.searchElement("ocean_world", "planet_class", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        // 文件级定义的元素应为 ParadoxScriptFile
        Assert.assertTrue(elements.single() is icu.windea.pls.script.psi.ParadoxScriptFile)
    }

    @Test
    fun test_SearchElement_NameField() {
        // Arrange
        markAndConfigureByFile("features/index/common/alien_species/00_species.txt")

        // Act: 用 name_field 的值搜索
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val elements = ParadoxDefinitionSearch.searchElement("zephyr_folk", "alien_species", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        // 对应的 PSI 属性键是 "zephyrian"
        Assert.assertEquals("zephyrian", elements.single().name)
    }

    // endregion

    // region Search with File Scope

    @Test
    fun test_WithFileScope() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        val starshipsFile = myFixture.file.virtualFile
        markAndConfigureByFile("features/index/common/drives/00_drives.txt")

        // Act: 仅搜索 starships 文件
        val fileScope = GlobalSearchScope.fileScope(project, starshipsFile)
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(fileScope)
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 仅包含 starship 定义
        Assert.assertEquals(3, results.size)
        Assert.assertTrue(results.all { it.type == "starship" })
    }

    // endregion

    // region Search Skip Root Key

    @Test
    fun test_SkipRootKey() {
        // Arrange
        markAndConfigureByFile("features/index/common/space_stations/00_stations.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "space_station", selector).findAll()

        // Assert: 跳过 "stations" 顶级键，索引其下的子定义
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("orbital_hub", "defense_platform"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search Anonymous

    @Test
    fun test_Anonymous() {
        // Arrange
        markAndConfigureByFile("features/index/common/star_systems/00_systems.txt")

        // Act: 按类型搜索匿名定义
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "star_system", selector).findAll()

        // Assert: 匿名定义也能通过类型查找
        Assert.assertEquals(2, results.size)
        Assert.assertTrue(results.all { it.name.isEmpty() })
        Assert.assertTrue(results.all { it.type == "star_system" })
    }

    @Test
    fun test_Anonymous_ByNameReturnsNothing() {
        // Arrange
        markAndConfigureByFile("features/index/common/star_systems/00_systems.txt")

        // Act: 用属性键名搜索不应返回匿名定义
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("sol", "star_system", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search Starts With

    @Test
    fun test_StartsWith() {
        // Arrange
        markAndConfigureByFile("features/index/common/districts/00_districts.txt")

        // Act: 用去除前缀后的名称搜索
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("city", "district", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("city", result.name)
        Assert.assertEquals("district", result.type)
        Assert.assertEquals("d_city", result.typeKey)
    }

    @Test
    fun test_StartsWith_AllByType() {
        // Arrange
        markAndConfigureByFile("features/index/common/districts/00_districts.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "district", selector).findAll()

        // Assert
        Assert.assertEquals(3, results.size)
        Assert.assertEquals(setOf("city", "mining", "generator"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search Type Key Regex

    @Test
    fun test_TypeKeyRegex() {
        // Arrange
        markAndConfigureByFile("features/index/common/fleets/00_fleets.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "fleet_template", selector).findAll()

        // Assert: 仅匹配正则的定义
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("fleet_assault", "fleet_patrol"), results.map { it.name }.toSet())
    }

    @Test
    fun test_TypeKeyRegex_ByName() {
        // Arrange
        markAndConfigureByFile("features/index/common/fleets/00_fleets.txt")

        // Act: 不匹配正则的属性名搜索不到结果
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("solo_corvette", "fleet_template", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search Skip Root Key Alternatives

    @Test
    fun test_SkipRootKey_Alternatives() {
        // Arrange
        markAndConfigureByFile("features/index/common/garrisons/00_garrisons.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "garrison", selector).findAll()

        // Assert: 跨两种根键，所有子定义均可搜到
        Assert.assertEquals(3, results.size)
        Assert.assertEquals(setOf("militia", "elite_guard", "coastal_battery"), results.map { it.name }.toSet())
    }

    // endregion

    // region Search Name Field Dash

    @Test
    fun test_NameFieldDash() {
        // Arrange
        markAndConfigureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act: 用属性值（name_field = "-"）搜索
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("alien_signal", "anomaly", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("alien_signal", result.name)
        Assert.assertEquals("anomaly_1", result.typeKey)
    }

    @Test
    fun test_NameFieldDash_TypeKeyNotSearchable() {
        // Arrange
        markAndConfigureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act: 用属性键搜索应返回空（因为 name 取自属性值）
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxDefinitionSearch.search("anomaly_1", "anomaly", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region searchFile / searchProperty

    @Test
    fun test_SearchFile_TypePerFile() {
        // Arrange
        markAndConfigureByFile("features/index/common/planet_classes/ocean_world.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val files = ParadoxDefinitionSearch.searchFile("ocean_world", "planet_class", selector).findAll()

        // Assert: 文件级定义通过 searchFile 可获取 PsiFile
        Assert.assertEquals(1, files.size)
        Assert.assertTrue(files.single() is icu.windea.pls.script.psi.ParadoxScriptFile)
    }

    @Test
    fun test_SearchFile_PropertyDefinition() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act: 属性级定义通过 searchFile 应返回空
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val files = ParadoxDefinitionSearch.searchFile("explorer", "starship", selector).findAll()

        // Assert
        Assert.assertTrue(files.isEmpty())
    }

    @Test
    fun test_SearchProperty() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val properties = ParadoxDefinitionSearch.searchProperty("explorer", "starship", selector).findAll()

        // Assert: 属性级定义通过 searchProperty 可获取 ParadoxScriptProperty
        Assert.assertEquals(1, properties.size)
        Assert.assertEquals("explorer", properties.single().name)
    }

    @Test
    fun test_SearchProperty_FileDefinition() {
        // Arrange
        markAndConfigureByFile("features/index/common/planet_classes/ocean_world.txt")

        // Act: 文件级定义通过 searchProperty 应返回空
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val properties = ParadoxDefinitionSearch.searchProperty("ocean_world", "planet_class", selector).findAll()

        // Assert
        Assert.assertTrue(properties.isEmpty())
    }

    // endregion

    // region Comprehensive Cross-Type Search

    @Test
    fun test_Comprehensive_AllTypes() {
        // Arrange: 加载所有类型的测试文件
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        markAndConfigureByFile("features/index/common/planet_classes/ocean_world.txt")
        markAndConfigureByFile("features/index/common/planet_classes/desert_world.txt")
        markAndConfigureByFile("features/index/common/alien_species/00_species.txt")
        markAndConfigureByFile("features/index/common/star_systems/00_systems.txt")
        markAndConfigureByFile("features/index/common/space_stations/00_stations.txt")
        markAndConfigureByFile("features/index/common/drives/00_drives.txt")
        markAndConfigureByFile("features/index/common/districts/00_districts.txt")
        markAndConfigureByFile("features/index/common/fleets/00_fleets.txt")
        markAndConfigureByFile("features/index/common/garrisons/00_garrisons.txt")
        markAndConfigureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
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
    fun test_Comprehensive_MixedSourceTypes() {
        // Arrange: 加载文件级和属性级定义
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        markAndConfigureByFile("features/index/common/planet_classes/ocean_world.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
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
    fun test_Comprehensive_MultiFileScope() {
        // Arrange
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        val starshipsFile = myFixture.file.virtualFile
        markAndConfigureByFile("features/index/common/districts/00_districts.txt")
        val districtsFile = myFixture.file.virtualFile
        markAndConfigureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act: 仅搜索 starships + districts 两个文件
        val unionScope = GlobalSearchScope.filesScope(project, listOf(starshipsFile, districtsFile))
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(unionScope)
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert: 3 starship + 3 district = 6（排除 anomaly）
        Assert.assertEquals(6, results.size)
        Assert.assertEquals(setOf("starship", "district"), results.map { it.type }.toSet())
    }

    // endregion

    // region Edge Cases

    @Test
    fun test_EmptyFile() {
        // Arrange: 文件仅含注释
        markAndConfigureByFile("features/index/common/starships/02_empty.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, null, selector).findAll()

        // Assert
        Assert.assertTrue(results.isEmpty())
    }

    @Test
    fun test_SearchElement_StartsWith() {
        // Arrange
        markAndConfigureByFile("features/index/common/districts/00_districts.txt")

        // Act: 用去前缀的 name 搜索 element
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val elements = ParadoxDefinitionSearch.searchElement("city", "district", selector).findAll()

        // Assert: PSI 元素的属性键保留完整前缀
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("d_city", elements.single().name)
    }

    @Test
    fun test_SearchElement_NameFieldDash() {
        // Arrange
        markAndConfigureByFile("features/index/common/anomalies/00_anomalies.txt")

        // Act: 用属性值（name_field="-"的 name）搜索 element
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val elements = ParadoxDefinitionSearch.searchElement("alien_signal", "anomaly", selector).findAll()

        // Assert: PSI 元素的属性键是 typeKey（anomaly_1），而非 name
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("anomaly_1", elements.single().name)
    }

    @Test
    fun test_ByNameOnly_AcrossMultipleTypes() {
        // Arrange: 加载多种类型的文件
        markAndConfigureByFile("features/index/common/starships/00_starships.txt")
        markAndConfigureByFile("features/index/common/drives/00_drives.txt")
        markAndConfigureByFile("features/index/common/districts/00_districts.txt")

        // Act: 仅按名称搜索，不指定类型
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
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
    fun test_DefinitionInjection_ReplaceOrCreate() {
        // Arrange: REPLACE_OR_CREATE 模式的定义注入应被索引为定义
        markAndConfigureByFile("features/index/common/arcane_tomes/01_inject.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
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
    fun test_DefinitionInjection_NonDefinitionModes_NotIndexed() {
        // Arrange: INJECT/REPLACE/TRY_INJECT 等非 create_mode 不应被索引为定义
        markAndConfigureByFile("features/index/common/arcane_tomes/01_inject.txt")

        // Act
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
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

    // region Search By Subtypes (inherited subtypes)

    @Test
    fun test_BySubtypes_Basic() {
        // Arrange: 加载事件测试数据
        markAndConfigureByFile("features/index/events/00_events.txt")

        // Act: 按 country 子类型搜索（使用类型表达式格式 "type.subtype"）
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "event.country", selector).findAll()

        // Assert: parent_event 和 child_event 都是 country_event
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("test.parent_event", "test.child_event"), results.map { it.name }.toSet())
    }

    @Test
    fun test_BySubtypes_InheritedFromParent() {
        // Arrange: 加载事件测试数据
        markAndConfigureByFile("features/index/events/00_events.txt")

        // Act: 按 triggered 子类型搜索（使用类型表达式格式 "type.subtype"）
        // parent_event 有 is_triggered_only = yes，所以有 triggered 子类型
        // child_event 继承自 parent_event，应该继承 triggered 子类型
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "event.triggered", selector).findAll()

        // Assert: parent_event 和 child_event 都应该有 triggered 子类型
        // child_event 的 triggered 子类型是从 parent_event 继承的
        Assert.assertTrue("parent_event should have triggered subtype", results.any { it.name == "test.parent_event" })
        Assert.assertTrue("child_event should inherit triggered subtype from parent_event", results.any { it.name == "test.child_event" })
    }

    @Test
    fun test_BySubtypes_NotInherited() {
        // Arrange: 加载事件测试数据
        markAndConfigureByFile("features/index/events/00_events.txt")

        // Act: simple_event 没有 triggered 子类型
        // child_simple_event 继承自 simple_event，也不应该有 triggered 子类型
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "event.triggered", selector).findAll()

        // Assert: simple_event 和 child_simple_event 都不应该在结果中
        Assert.assertFalse("simple_event should not have triggered subtype", results.any { it.name == "test.simple_event" })
        Assert.assertFalse("child_simple_event should not have triggered subtype", results.any { it.name == "test.child_simple_event" })
    }

    @Test
    fun test_BySubtypes_MultipleSubtypes() {
        // Arrange: 加载事件测试数据
        markAndConfigureByFile("features/index/events/00_events.txt")

        // Act: 按 country + triggered 两个子类型搜索（使用类型表达式格式 "type.subtype1.subtype2"）
        val selector = ParadoxDefinitionSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDefinitionSearch.search(null, "event.country.triggered", selector).findAll()

        // Assert: 只有 parent_event 和 child_event 同时满足两个条件
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("test.parent_event", "test.child_event"), results.map { it.name }.toSet())
    }

    // endregion
}

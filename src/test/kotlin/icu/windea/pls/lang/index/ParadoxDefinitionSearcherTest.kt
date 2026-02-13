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

    private fun configureScriptFile(relPath: String, @TestDataFile testDataPath: String) {
        markFileInfo(ParadoxGameType.Stellaris, relPath)
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
}

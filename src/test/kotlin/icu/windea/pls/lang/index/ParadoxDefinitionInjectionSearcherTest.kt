package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
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
class ParadoxDefinitionInjectionSearcherTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Vic3

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
    fun testDefinitionInjectionSearcher_All() {
        // Arrange: 加载所有注入文件
        configureScriptFile("common/ai_strategies/00_default.txt", "features/index/common/ai_strategies/00_default.txt")
        configureScriptFile("common/ai_strategies/01_inject.txt", "features/index/common/ai_strategies/01_inject.txt")
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, null, null, selector).findAll()

        // Assert: ai_strategy(1) + arcane_tome(4) + academy_spell(2) = 7
        Assert.assertEquals(7, results.size)
        Assert.assertTrue(results.any { it.target == "ai_strategy_default" && it.type == "ai_strategy" && it.mode == "INJECT" })
        Assert.assertTrue(results.any { it.target == "tome_of_flames" && it.type == "arcane_tome" && it.mode == "INJECT" })
        Assert.assertTrue(results.any { it.target == "tome_of_ice" && it.type == "arcane_tome" && it.mode == "REPLACE" })
        Assert.assertTrue(results.any { it.target == "tome_of_new" && it.type == "arcane_tome" && it.mode == "REPLACE_OR_CREATE" })
        Assert.assertTrue(results.any { it.target == "shared_name" && it.type == "arcane_tome" && it.mode == "TRY_INJECT" })
        Assert.assertTrue(results.any { it.target == "shared_name" && it.type == "academy_spell" && it.mode == "INJECT" })
        Assert.assertTrue(results.any { it.target == "spell_of_mists" && it.type == "academy_spell" && it.mode == "INJECT" })
    }

    // endregion

    // region Search By Target

    @Test
    fun testDefinitionInjectionSearcher_ByTarget() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionInjectionSearch.search(null, "tome_of_flames", null, selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("tome_of_flames", result.target)
        Assert.assertEquals("arcane_tome", result.type)
        Assert.assertEquals("INJECT", result.mode)
    }

    @Test
    fun testDefinitionInjectionSearcher_ByTarget_AcrossTypes() {
        // Arrange: 同一 target 出现在不同类型中
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, "shared_name", null, selector).findAll()

        // Assert: 两种类型均包含
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("arcane_tome", "academy_spell"), results.map { it.type }.toSet())
    }

    @Test
    fun testDefinitionInjectionSearcher_ByTarget_NotFound() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 搜索不存在的 target
        val result = ParadoxDefinitionInjectionSearch.search(null, "nonexistent_target", null, selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search By Type

    @Test
    fun testDefinitionInjectionSearcher_ByType() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, null, "arcane_tome", selector).findAll()

        // Assert
        Assert.assertEquals(4, results.size)
        Assert.assertTrue(results.all { it.type == "arcane_tome" })
        Assert.assertEquals(
            setOf("tome_of_flames", "tome_of_ice", "tome_of_new", "shared_name"),
            results.map { it.target }.toSet()
        )
    }

    // endregion

    // region Search By Target And Type

    @Test
    fun testDefinitionInjectionSearcher_ByTargetAndType() {
        // Arrange
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionInjectionSearch.search(null, "shared_name", "academy_spell", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        result!!
        Assert.assertEquals("shared_name", result.target)
        Assert.assertEquals("academy_spell", result.type)
        Assert.assertEquals("INJECT", result.mode)
    }

    @Test
    fun testDefinitionInjectionSearcher_ByTargetAndType_Mismatch() {
        // Arrange: target 存在但类型不匹配
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionInjectionSearch.search(null, "tome_of_flames", "academy_spell", selector).findFirst()

        // Assert
        Assert.assertNull(result)
    }

    // endregion

    // region Search By Mode

    @Test
    fun testDefinitionInjectionSearcher_ByMode_CaseInsensitive() {
        // Arrange: mode 参数大小写不敏感
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionInjectionSearch.search("replace", "tome_of_ice", "arcane_tome", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("REPLACE", result!!.mode)
    }

    @Test
    fun testDefinitionInjectionSearcher_ByMode_FilterResults() {
        // Arrange: 同一类型中不同 mode 的结果应被过滤
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 仅搜索 INJECT 模式
        val results = ParadoxDefinitionInjectionSearch.search("INJECT", null, "arcane_tome", selector).findAll()

        // Assert: 仅返回 INJECT 模式的结果（tome_of_flames），排除 REPLACE 和 TRY_INJECT
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("tome_of_flames", results.single().target)
        Assert.assertEquals("INJECT", results.single().mode)
    }

    // endregion

    // region Search Element

    @Test
    fun testDefinitionInjectionSearcher_SearchElement() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val elements = ParadoxDefinitionInjectionSearch.searchElement(null, "tome_of_flames", "arcane_tome", selector).findAll()

        // Assert: PSI 属性名包含 mode 前缀
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("INJECT:tome_of_flames", elements.single().name)
    }

    @Test
    fun testDefinitionInjectionSearcher_SearchElement_Replace() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act: 搜索 REPLACE 模式的元素
        val elements = ParadoxDefinitionInjectionSearch.searchElement("REPLACE", "tome_of_ice", "arcane_tome", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("REPLACE:tome_of_ice", elements.single().name)
    }

    // endregion

    // region Search with File Scope

    @Test
    fun testDefinitionInjectionSearcher_WithFileScope() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        val arcaneFile = myFixture.file.virtualFile
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection()
            .withSearchScope(GlobalSearchScope.fileScope(project, arcaneFile))

        // Act: 仅搜索 arcane_tomes 文件
        val results = ParadoxDefinitionInjectionSearch.search(null, "shared_name", null, selector).findAll()

        // Assert: 仅包含 arcane_tome 类型的结果
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("arcane_tome", results.single().type)
    }

    @Test
    fun testDefinitionInjectionSearcher_WithFileScope_MultiFile() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        val arcaneFile = myFixture.file.virtualFile
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")
        val spellsFile = myFixture.file.virtualFile
        configureScriptFile("common/ai_strategies/01_inject.txt", "features/index/common/ai_strategies/01_inject.txt")

        // Act: 仅搜索 arcane_tomes + academy_spells 两个文件
        val unionScope = GlobalSearchScope.filesScope(project, listOf(arcaneFile, spellsFile))
        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(unionScope)
        val results = ParadoxDefinitionInjectionSearch.search(null, null, null, selector).findAll()

        // Assert: arcane_tome(4) + academy_spell(2) = 5（排除 ai_strategy）
        Assert.assertEquals(6, results.size)
        Assert.assertEquals(setOf("arcane_tome", "academy_spell"), results.map { it.type }.toSet())
    }

    // endregion
}

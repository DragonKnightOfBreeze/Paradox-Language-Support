package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
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
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, ParadoxGameType.Vic3)
    }

    @After
    fun clear() = clearIntegrationTest()

    private fun configureScriptFile(relPath: String, @TestDataFile testDataPath: String) {
        markFileInfo(ParadoxGameType.Vic3, relPath)
        myFixture.configureByFile(testDataPath)
    }

    private fun Collection<ParadoxDefinitionInjectionIndexInfo>.sorted(): List<ParadoxDefinitionInjectionIndexInfo> {
        return sortedWith(compareBy({ it.type }, { it.target }, { it.mode }))
    }

    @Test
    fun testDefinitionInjectionSearcher_All() {
        // Arrange
        configureScriptFile("common/ai_strategies/00_default.txt", "features/index/common/ai_strategies/00_default.txt")
        configureScriptFile("common/ai_strategies/01_inject.txt", "features/index/common/ai_strategies/01_inject.txt")
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, null, null, selector).findAll()

        // Assert
        Assert.assertEquals(6, results.size)
        Assert.assertTrue(results.any { it.target == "ai_strategy_default" && it.type == "ai_strategy" && it.mode == "INJECT" })
        Assert.assertTrue(results.any { it.target == "tome_of_flames" && it.type == "arcane_tome" && it.mode == "INJECT" })
        Assert.assertTrue(results.any { it.target == "tome_of_ice" && it.type == "arcane_tome" && it.mode == "REPLACE" })
        Assert.assertTrue(results.any { it.target == "shared_name" && it.type == "arcane_tome" && it.mode == "TRY_INJECT" })
        Assert.assertTrue(results.any { it.target == "shared_name" && it.type == "academy_spell" && it.mode == "INJECT" })
        Assert.assertTrue(results.any { it.target == "spell_of_mists" && it.type == "academy_spell" && it.mode == "INJECT" })
    }

    @Test
    fun testDefinitionInjectionSearcher_ByTargetAcrossTypes() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, "shared_name", null, selector).findAll()
        // Assert
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(listOf("academy_spell", "arcane_tome"), results.sorted().map { it.type })
    }

    @Test
    fun testDefinitionInjectionSearcher_ByType() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, null, "arcane_tome", selector).findAll()

        // Assert
        Assert.assertEquals(3, results.size)
        Assert.assertTrue(results.all { it.type == "arcane_tome" })
    }

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
    fun testDefinitionInjectionSearcher_ByMode_CaseInsensitive() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxDefinitionInjectionSearch.search("replace", "tome_of_ice", "arcane_tome", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("REPLACE", result!!.mode)
    }

    @Test
    fun testDefinitionInjectionSearcher_SearchElement() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val elements = ParadoxDefinitionInjectionSearch.searchElement(null, "tome_of_flames", "arcane_tome", selector).findAll()

        // Assert
        Assert.assertEquals(1, elements.size)
        Assert.assertEquals("INJECT:tome_of_flames", elements.single().name)
    }

    @Test
    fun testDefinitionInjectionSearcher_WithFileScope() {
        // Arrange
        configureScriptFile("common/arcane_tomes/01_inject.txt", "features/index/common/arcane_tomes/01_inject.txt")
        val arcaneFile = myFixture.file.virtualFile
        configureScriptFile("common/academy_spells/01_inject.txt", "features/index/common/academy_spells/01_inject.txt")

        val selector = selector(project, myFixture.file).definitionInjection()
            .withSearchScope(GlobalSearchScope.fileScope(project, arcaneFile))

        // Act
        val results = ParadoxDefinitionInjectionSearch.search(null, "shared_name", null, selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("arcane_tome", results.single().type)
    }

}

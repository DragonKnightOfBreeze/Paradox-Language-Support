package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.search.selector.withSearchScopeType
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
class ParadoxComplexEnumValueSearcherTest : BasePlatformTestCase() {
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

    private fun configureScriptFile(relPath: String, testDataPath: String) {
        markFileInfo(ParadoxGameType.Vic3, relPath)
        myFixture.configureByFile(testDataPath)
    }

    @Test
    fun testComplexEnumValueSearcher_ByEnumName() {
        // Arrange
        configureScriptFile("common/spell_schools/00_spell_schools.txt", "features/index/common/spell_schools/00_spell_schools.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxComplexEnumValueSearch.search(null, "spell_school", selector).findAll().toList()

        // Assert
        Assert.assertEquals(setOf("evocation", "illusion", "necromancy"), results.map { it.name }.toSet())
    }

    @Test
    fun testComplexEnumValueSearcher_ByName_CaseInsensitive() {
        // Arrange
        configureScriptFile("common/whispered_words/00_words.txt", "features/index/common/whispered_words/00_words.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxComplexEnumValueSearch.search("hush", "whispered_word", selector).findAll().toList()

        // Assert
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("Hush", "hush"), results.map { it.name }.toSet())
    }

    @Test
    fun testComplexEnumValueSearcher_NestedMatch() {
        // Arrange
        configureScriptFile("common/arcane_tomes/03_complex_enum.txt", "features/index/common/arcane_tomes/03_complex_enum.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val result = ParadoxComplexEnumValueSearch.search("fire", "tome_tag", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("fire", result!!.name)
        Assert.assertEquals("tome_tag", result.enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_PerDefinition_WithDefinitionScope() {
        // Arrange
        configureScriptFile("common/arcane_tomes/04_per_definition.txt", "features/index/common/arcane_tomes/04_per_definition.txt")
        val text = myFixture.file.text
        val alphaIndex = text.indexOf("alpha")
        Assert.assertTrue(alphaIndex >= 0)
        val contextElement = myFixture.file.findElementAt(alphaIndex + 1)!!

        val selector = selector(project, contextElement).complexEnumValue()
            .withSearchScope(GlobalSearchScope.projectScope(project))
            .withSearchScopeType("definition")

        // Act
        val results = ParadoxComplexEnumValueSearch.search("alpha", "ritual_phrase", selector).findAll().toList()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("alpha", results.single().name)
        Assert.assertEquals("ritual_phrase", results.single().enumName)
        Assert.assertTrue(results.single().definitionElementOffset != -1)
    }

    @Test
    fun testComplexEnumValueSearcher_PerDefinition_AllDefinitions() {
        // Arrange
        configureScriptFile("common/arcane_tomes/04_per_definition.txt", "features/index/common/arcane_tomes/04_per_definition.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        // Act
        val results = ParadoxComplexEnumValueSearch.search("alpha", "ritual_phrase", selector).findAll().toList()

        // Assert
        Assert.assertEquals(2, results.size)
        Assert.assertTrue(results.all { it.definitionElementOffset != -1 })
        Assert.assertEquals(2, results.map { it.definitionElementOffset }.toSet().size)
    }
}

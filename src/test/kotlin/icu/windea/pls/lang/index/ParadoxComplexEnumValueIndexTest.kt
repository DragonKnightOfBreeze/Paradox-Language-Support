package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
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
class ParadoxComplexEnumValueIndexTest : BasePlatformTestCase() {
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

    @Test
    fun testComplexEnumValueIndex_Basic_TopLevelValues() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/spell_schools/00_spell_schools.txt")
        myFixture.configureByFile("features/index/common/spell_schools/00_spell_schools.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("spell_school")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        // Assert
        val expectedNames = setOf("evocation", "illusion", "necromancy")
        Assert.assertEquals(expectedNames, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "spell_school" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
        Assert.assertTrue(values.all { it.gameType == ParadoxGameType.Vic3 })
    }

    @Test
    fun testComplexEnumValueIndex_Nested_ByScalarMatch() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/arcane_tomes/03_complex_enum.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/03_complex_enum.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("tome_tag")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        // Assert
        val expectedNames = setOf("fire", "ice")
        Assert.assertEquals(expectedNames, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "tome_tag" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_CaseInsensitiveFlagAndCompression() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/whispered_words/00_words.txt")
        myFixture.configureByFile("features/index/common/whispered_words/00_words.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("whispered_word")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        // Assert
        Assert.assertEquals(listOf("Hush", "Murmur", "hush"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "whispered_word" })
        Assert.assertTrue(values.all { it.caseInsensitive })
    }

    @Test
    fun testComplexEnumValueIndex_PerDefinitionAndSkipParameterized() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/arcane_tomes/04_per_definition.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/04_per_definition.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("ritual_phrase")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        // Assert
        Assert.assertEquals(listOf("alpha", "alpha"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "ritual_phrase" })
        Assert.assertTrue(values.all { it.definitionElementOffset != -1 })
        Assert.assertEquals(2, values.map { it.definitionElementOffset }.toSet().size)
    }
}

package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.util.withSearchScope
import icu.windea.pls.lang.search.util.withSearchScopeType
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
 * @see ParadoxComplexEnumValueSearch
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxComplexEnumValueSearchTest : BasePlatformTestCase() {
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

    // region Basic

    @Test
    fun test_ByEnumName() {
        // Arrange
        markAndConfigureByFile("features/index/common/spell_schools/00_spell_schools.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search(null, "spell_school", selector).findAll()

        // Assert
        Assert.assertEquals(setOf("evocation", "illusion", "necromancy"), results.map { it.name }.toSet())
    }

    @Test
    fun test_ByName_CaseInsensitive() {
        // Arrange
        markAndConfigureByFile("features/index/common/whispered_words/00_words.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("hush", "whispered_word", selector).findAll()

        // Assert
        Assert.assertEquals(2, results.size)
        Assert.assertEquals(setOf("Hush", "hush"), results.map { it.name }.toSet())
    }

    // endregion

    // region Config Flags

    @Test
    fun test_NestedMatch() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/03_complex_enum.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val result = ParadoxComplexEnumValueSearch.search("fire", "tome_tag", selector).findFirst()

        // Assert
        Assert.assertNotNull(result)
        Assert.assertEquals("fire", result!!.name)
        Assert.assertEquals("tome_tag", result.enumName)
    }

    @Test
    fun test_PerDefinition_WithDefinitionScope() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/04_per_definition.txt")

        val text = myFixture.file.text
        val alphaIndex = text.indexOf("alpha")
        Assert.assertTrue(alphaIndex >= 0)
        val contextElement = myFixture.file.findElementAt(alphaIndex + 1)!!

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, contextElement)
            .withSearchScope(GlobalSearchScope.projectScope(project))
            .withSearchScopeType("definition")
        val results = ParadoxComplexEnumValueSearch.search("alpha", "ritual_phrase", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("alpha", results.single().name)
        Assert.assertEquals("ritual_phrase", results.single().enumName)
        Assert.assertTrue(results.single().definitionElementOffset != -1)
    }

    @Test
    fun test_PerDefinition_AllDefinitions() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/04_per_definition.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("alpha", "ritual_phrase", selector).findAll()

        // Assert
        Assert.assertEquals(2, results.size)
        Assert.assertTrue(results.all { it.definitionElementOffset != -1 })
        Assert.assertEquals(2, results.map { it.definitionElementOffset }.toSet().size)
    }

    // endregion

    // region Enum Name Source

    @Test
    fun test_StartFromRootNo_ValueInBlockOnly() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("list_enum_entry", "list_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("list_enum_entry", results.single().name)
        Assert.assertEquals("list_enum", results.single().enumName)
    }

    @Test
    fun test_EnumNameAsPropertyKey() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("key_enum_entry", "key_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("key_enum_entry", results.single().name)
        Assert.assertEquals("key_enum", results.single().enumName)
    }

    // endregion

    // region Structural Constraints

    @Test
    fun test_DeepEnum_MultiLevelBlockAndFilterProperty() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("deep_enum_value", "deep_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("deep_enum_value", results.single().name)
        Assert.assertEquals("deep_enum", results.single().enumName)
    }

    @Test
    fun test_EnumNameAsPropertyValue() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("prop_value_enum_value", "prop_value_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("prop_value_enum_value", results.single().name)
        Assert.assertEquals("prop_value_enum", results.single().enumName)
    }

    @Test
    fun test_SiblingEnum_MultiPropertyConstraints() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("sibling_enum_value", "sibling_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("sibling_enum_value", results.single().name)
        Assert.assertEquals("sibling_enum", results.single().enumName)
    }

    @Test
    fun test_KeyBlockEnum_PropertyKeyWithBlockConstraint() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("key_block_enum_value", "key_block_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("key_block_enum_value", results.single().name)
        Assert.assertEquals("key_block_enum", results.single().enumName)
    }

    @Test
    fun test_MixEnum_MixedKeyValueBlock() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("mix_key", "mix_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("mix_key", results.single().name)
        Assert.assertEquals("mix_enum", results.single().enumName)
    }

    @Test
    fun test_TypeComboEnum_TypedConstraints() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("type_combo_value", "type_combo_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("type_combo_value", results.single().name)
        Assert.assertEquals("type_combo_enum", results.single().enumName)
    }

    @Test
    fun test_MultiEnum_MultipleEnumNameConfigs() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("multi_first", "multi_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("multi_first", results.single().name)
        Assert.assertEquals("multi_enum", results.single().enumName)
    }

    @Test
    fun test_ValueMixEnum_ValueEnumNameAndTypedConstraints() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("value_mix_entry", "value_mix_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("value_mix_entry", results.single().name)
        Assert.assertEquals("value_mix_enum", results.single().enumName)
    }

    // endregion

    // region Combined Features

    @Test
    fun test_CaseInsensitiveEnum_ComplexStructure() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("ci_entry", "case_insensitive_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Ci_Entry", results.single().name)
        Assert.assertEquals("case_insensitive_enum", results.single().enumName)
    }

    @Test
    fun test_MultiLevelEnum_MultipleLevels() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("outer_entry", "multi_level_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("outer_entry", results.single().name)
        Assert.assertEquals("multi_level_enum", results.single().enumName)
    }

    @Test
    fun test_CrossLevelMixEnum_CrossLevelConstraints() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("cross_outer", "cross_level_mix_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("cross_outer", results.single().name)
        Assert.assertEquals("cross_level_mix_enum", results.single().enumName)
    }

    @Test
    fun test_CaseInsensitiveMultiEnum_ComplexAndTyped() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("ci_multi_outer", "case_insensitive_multi_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Ci_Multi_Outer", results.single().name)
        Assert.assertEquals("case_insensitive_multi_enum", results.single().enumName)
    }

    @Test
    fun test_MultiLevelMultiEnum_MultipleLevels() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("level_a_entry", "multi_level_multi_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("level_a_entry", results.single().name)
        Assert.assertEquals("multi_level_multi_enum", results.single().enumName)
    }

    @Test
    fun test_CrossPvbEnum_CrossLevelPropertyValueBlock() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("pvb_key", "cross_pvb_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("pvb_key", results.single().name)
        Assert.assertEquals("cross_pvb_enum", results.single().enumName)
    }

    @Test
    fun test_CaseInsensitiveDeepEnum_MultipleLevels() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("ci_deep_outer", "case_insensitive_deep_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Ci_Deep_Outer", results.single().name)
        Assert.assertEquals("case_insensitive_deep_enum", results.single().enumName)
    }

    @Test
    fun test_RepeatTypedEnum_RepeatedEnumNameAndTypedConstraints() {
        // Arrange
        markAndConfigureByFile("features/index/common/arcane_tomes/00_base.txt")

        // Act
        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("repeat_first", "repeat_typed_enum", selector).findAll()

        // Assert
        Assert.assertEquals(1, results.size)
        Assert.assertEquals("repeat_first", results.single().name)
        Assert.assertEquals("repeat_typed_enum", results.single().enumName)
    }

    // endregion

    // region Grimoires

    @Test
    fun testGrimoires_FromColumn() {
        markFileInfo(gameType, "common/grimoire_tocs/00_grimoire_tocs.csv")
        myFixture.configureByFile("features/index/common/grimoire_tocs/00_grimoire_tocs.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search("auther_saber", "magic_name", selector).findAll()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("auther_saber", results.single().name)
        Assert.assertEquals("magic_name", results.single().enumName)
    }

    @Test
    fun testGrimoires_FromColumn_FindAll() {
        markFileInfo(gameType, "common/grimoire_tocs/00_grimoire_tocs.csv")
        myFixture.configureByFile("features/index/common/grimoire_tocs/00_grimoire_tocs.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val selector = ParadoxComplexEnumValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxComplexEnumValueSearch.search(null, "magic_name", selector).findAll()

        val magicNames = results.map { it.name }.toSet()
        assertNotEmpty(magicNames)

        val expected = setOf(
            "wind_blade",
            "tornado",
            "protective_wind",
            "auther_saber",
            "lunar_blade",
            "streaming_stream",
            "attacking_wind",
            "shining_spear",
            "inner_fire",
            "dragon_dance_at_dusk",
        )
        Assert.assertEquals(expected, magicNames)
    }

    // endregion
}

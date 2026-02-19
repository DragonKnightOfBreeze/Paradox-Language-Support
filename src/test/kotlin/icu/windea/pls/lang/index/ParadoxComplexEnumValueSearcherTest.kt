package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.TestDataFile
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

    // region Basic

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

    // endregion

    // region Config Flags

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

    // endregion

    // region Enum Name Source

    @Test
    fun testComplexEnumValueSearcher_StartFromRootNo_ValueInBlockOnly() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("list_enum_entry", "list_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("list_enum_entry", results.single().name)
        Assert.assertEquals("list_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_EnumNameAsPropertyKey() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("key_enum_entry", "key_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("key_enum_entry", results.single().name)
        Assert.assertEquals("key_enum", results.single().enumName)
    }

    // endregion

    // region Structural Constraints

    @Test
    fun testComplexEnumValueSearcher_DeepEnum_MultiLevelBlockAndFilterProperty() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("deep_enum_value", "deep_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("deep_enum_value", results.single().name)
        Assert.assertEquals("deep_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_EnumNameAsPropertyValue() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("prop_value_enum_value", "prop_value_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("prop_value_enum_value", results.single().name)
        Assert.assertEquals("prop_value_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_SiblingEnum_MultiPropertyConstraints() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("sibling_enum_value", "sibling_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("sibling_enum_value", results.single().name)
        Assert.assertEquals("sibling_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_KeyBlockEnum_PropertyKeyWithBlockConstraint() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("key_block_enum_value", "key_block_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("key_block_enum_value", results.single().name)
        Assert.assertEquals("key_block_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_MixEnum_MixedKeyValueBlock() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("mix_key", "mix_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("mix_key", results.single().name)
        Assert.assertEquals("mix_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_TypeComboEnum_TypedConstraints() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("type_combo_value", "type_combo_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("type_combo_value", results.single().name)
        Assert.assertEquals("type_combo_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_MultiEnum_MultipleEnumNameConfigs() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("multi_first", "multi_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("multi_first", results.single().name)
        Assert.assertEquals("multi_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_ValueMixEnum_ValueEnumNameAndTypedConstraints() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("value_mix_entry", "value_mix_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("value_mix_entry", results.single().name)
        Assert.assertEquals("value_mix_enum", results.single().enumName)
    }

    // endregion

    // region Combined Features

    @Test
    fun testComplexEnumValueSearcher_CaseInsensitiveEnum_ComplexStructure() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("ci_entry", "case_insensitive_enum", selector)
            .findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Ci_Entry", results.single().name)
        Assert.assertEquals("case_insensitive_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_MultiLevelEnum_MultipleLevels() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("outer_entry", "multi_level_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("outer_entry", results.single().name)
        Assert.assertEquals("multi_level_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_CrossLevelMixEnum_CrossLevelConstraints() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("cross_outer", "cross_level_mix_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("cross_outer", results.single().name)
        Assert.assertEquals("cross_level_mix_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_CaseInsensitiveMultiEnum_ComplexAndTyped() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("ci_multi_outer", "case_insensitive_multi_enum", selector)
            .findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Ci_Multi_Outer", results.single().name)
        Assert.assertEquals("case_insensitive_multi_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_MultiLevelMultiEnum_MultipleLevels() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("level_a_entry", "multi_level_multi_enum", selector)
            .findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("level_a_entry", results.single().name)
        Assert.assertEquals("multi_level_multi_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_CrossPvbEnum_CrossLevelPropertyValueBlock() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("pvb_key", "cross_pvb_enum", selector).findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("pvb_key", results.single().name)
        Assert.assertEquals("cross_pvb_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_CaseInsensitiveDeepEnum_MultipleLevels() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("ci_deep_outer", "case_insensitive_deep_enum", selector)
            .findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Ci_Deep_Outer", results.single().name)
        Assert.assertEquals("case_insensitive_deep_enum", results.single().enumName)
    }

    @Test
    fun testComplexEnumValueSearcher_RepeatTypedEnum_RepeatedEnumNameAndTypedConstraints() {
        configureScriptFile("common/arcane_tomes/00_base.txt", "features/index/common/arcane_tomes/00_base.txt")
        val selector = selector(project, myFixture.file).complexEnumValue().withSearchScope(GlobalSearchScope.projectScope(project))

        val results = ParadoxComplexEnumValueSearch.search("repeat_first", "repeat_typed_enum", selector)
            .findAll().toList()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("repeat_first", results.single().name)
        Assert.assertEquals("repeat_typed_enum", results.single().enumName)
    }

    // endregion
}

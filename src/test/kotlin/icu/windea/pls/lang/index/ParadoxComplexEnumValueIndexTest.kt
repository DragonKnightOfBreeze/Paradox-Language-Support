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

    // region Basic

    @Test
    fun testComplexEnumValueIndex_Basic_TopLevelValues() {
        // Arrange
        markFileInfo(gameType, "common/spell_schools/00_spell_schools.txt")
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
        Assert.assertTrue(values.all { it.gameType == gameType })
    }

    @Test
    fun testComplexEnumValueIndex_Nested_ByScalarMatch() {
        // Arrange
        markFileInfo(gameType, "common/arcane_tomes/03_complex_enum.txt")
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

    // endregion

    // region Config Flags

    @Test
    fun testComplexEnumValueIndex_CaseInsensitiveFlagAndCompression() {
        // Arrange
        markFileInfo(gameType, "common/whispered_words/00_words.txt")
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
        markFileInfo(gameType, "common/arcane_tomes/04_per_definition.txt")
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

    // endregion

    // region Enum Name Source

    @Test
    fun testComplexEnumValueIndex_StartFromRootNo_ValueInBlockOnly() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("list_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("list_enum_entry"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "list_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_EnumNameAsPropertyKey() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("key_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("key_enum_entry"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "key_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    // endregion

    // region Structural Constraints

    @Test
    fun testComplexEnumValueIndex_DeepEnum_MultiLevelBlockAndFilterProperty() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("deep_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("deep_enum_value"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "deep_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_EnumNameAsPropertyValue() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("prop_value_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("prop_value_enum_value"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "prop_value_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_SiblingEnum_MultiPropertyConstraints() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("sibling_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("sibling_enum_value"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "sibling_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_KeyBlockEnum_PropertyKeyWithBlockConstraint() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("key_block_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("key_block_enum_value"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "key_block_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_MixEnum_MixedKeyValueBlock() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("mix_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("mix_key", "mix_value", "mix_member")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "mix_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_TypeComboEnum_TypedConstraints() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("type_combo_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("type_combo_value"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "type_combo_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_MultiEnum_MultipleEnumNameConfigs() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("multi_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("multi_first", "multi_second")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "multi_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_ValueMixEnum_ValueEnumNameAndTypedConstraints() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("value_mix_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("value_mix_entry"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "value_mix_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    // endregion

    // region Combined Features

    @Test
    fun testComplexEnumValueIndex_CaseInsensitiveEnum_ComplexStructure() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("case_insensitive_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        Assert.assertEquals(listOf("Ci_Entry"), values.map { it.name })
        Assert.assertTrue(values.all { it.enumName == "case_insensitive_enum" })
        Assert.assertTrue(values.all { it.caseInsensitive })
    }

    @Test
    fun testComplexEnumValueIndex_MultiLevelEnum_MultipleLevels() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("multi_level_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("outer_entry", "inner_entry")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "multi_level_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_CrossLevelMixEnum_CrossLevelConstraints() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("cross_level_mix_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("cross_outer", "cross_inner")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "cross_level_mix_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_CaseInsensitiveMultiEnum_ComplexAndTyped() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("case_insensitive_multi_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("Ci_Multi_Outer", "Ci_Multi_Inner")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "case_insensitive_multi_enum" })
        Assert.assertTrue(values.all { it.caseInsensitive })
    }

    @Test
    fun testComplexEnumValueIndex_MultiLevelMultiEnum_MultipleLevels() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("multi_level_multi_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("level_a_entry", "level_b_entry", "level_c_entry")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "multi_level_multi_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_CrossPvbEnum_CrossLevelPropertyValueBlock() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("cross_pvb_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("pvb_key", "pvb_label", "pvb_member")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "cross_pvb_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    @Test
    fun testComplexEnumValueIndex_CaseInsensitiveDeepEnum_MultipleLevels() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("case_insensitive_deep_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("Ci_Deep_Outer", "Ci_Deep_Middle", "Ci_Deep_Inner")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "case_insensitive_deep_enum" })
        Assert.assertTrue(values.all { it.caseInsensitive })
    }

    @Test
    fun testComplexEnumValueIndex_RepeatTypedEnum_RepeatedEnumNameAndTypedConstraints() {
        markFileInfo(gameType, "common/arcane_tomes/00_base.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/00_base.txt")

        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val key = PlsIndexUtil.createTypeKey("repeat_typed_enum")
        val values = FileBasedIndex.getInstance().getValues(PlsIndexKeys.ComplexEnumValue, key, scope).flatten()

        val expected = setOf("repeat_first", "repeat_second")
        Assert.assertEquals(expected, values.map { it.name }.toSet())
        Assert.assertTrue(values.all { it.enumName == "repeat_typed_enum" })
        Assert.assertTrue(values.all { it.definitionElementOffset == -1 })
    }

    // endregion
}

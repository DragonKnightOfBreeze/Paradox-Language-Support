package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.startOffset
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
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
class ParadoxDefinitionInjectionIndexTest : BasePlatformTestCase() {
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

    // region Basic Injection

    @Test
    fun testDefinitionInjectionIndex_Basic() {
        // Arrange: 基础 INJECT 模式
        markFileInfo(gameType, "common/ai_strategies/00_default.txt")
        myFixture.configureByFile("features/index/common/ai_strategies/00_default.txt")
        markFileInfo(gameType, "common/ai_strategies/01_inject.txt")
        myFixture.configureByFile("features/index/common/ai_strategies/01_inject.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val allData = FileBasedIndex.getInstance().getValues(PlsIndexKeys.DefinitionInjection, PlsIndexUtil.createAllKey(), scope).flatten()

        // Assert
        val expect = listOf(
            ParadoxDefinitionInjectionIndexInfo("INJECT", "ai_strategy_default", "ai_strategy", 0, gameType)
        )
        Assert.assertEquals(expect, allData)
    }

    // endregion

    // region Multiple Modes

    @Test
    fun testDefinitionInjectionIndex_MultipleModes() {
        // Arrange: 同一文件中 INJECT / REPLACE / TRY_INJECT 三种模式
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")

        val properties = PsiTreeUtil.findChildrenOfType(psiFile, ParadoxScriptProperty::class.java)
        val injectFlames = properties.single { it.name == "INJECT:tome_of_flames" }
        val replaceIce = properties.single { it.name == "REPLACE:tome_of_ice" }
        val new = properties.single { it.name == "REPLACE_OR_CREATE:tome_of_new" }
        val tryInjectShared = properties.single { it.name == "TRY_INJECT:shared_name" }

        val expectedInfos = listOf(
            ParadoxDefinitionInjectionIndexInfo("INJECT", "tome_of_flames", "arcane_tome", injectFlames.startOffset, gameType),
            ParadoxDefinitionInjectionIndexInfo("REPLACE", "tome_of_ice", "arcane_tome", replaceIce.startOffset, gameType),
            ParadoxDefinitionInjectionIndexInfo("REPLACE_OR_CREATE", "tome_of_new", "arcane_tome", new.startOffset, gameType),
            ParadoxDefinitionInjectionIndexInfo("TRY_INJECT", "shared_name", "arcane_tome", tryInjectShared.startOffset, gameType),
        )

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert: key 分发
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        Assert.assertEquals(4, allInfos.size)
        Assert.assertEquals(expectedInfos.toSet(), allInfos.toSet())

        val typeInfos = fileData[PlsIndexUtil.createTypeKey("arcane_tome")].orEmpty()
        Assert.assertEquals(4, typeInfos.size)

        Assert.assertEquals(listOf(expectedInfos[0]), fileData[PlsIndexUtil.createNameKey("tome_of_flames")])
        Assert.assertEquals(listOf(expectedInfos[0]), fileData[PlsIndexUtil.createNameTypeKey("tome_of_flames", "arcane_tome")])
    }

    @Test
    fun testDefinitionInjectionIndex_MultipleModes_IgnoredCases() {
        // Arrange: 验证非法写法被正确忽略
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert: 标量值（非块）应被忽略
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("tome_scalar")])
        // 未知模式应被忽略
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("should_be_ignored")])
        // 参数化表达式应被忽略
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("A_\$PARAM\$_B")])
        // 嵌套属性（非顶层）应被忽略
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("nested_should_be_ignored")])
    }

    // endregion

    // region Different Types

    @Test
    fun testDefinitionInjectionIndex_DifferentTypes_ByFilePath() {
        // Arrange: 不同路径对应不同类型
        markFileInfo(gameType, "common/academy_spells/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/academy_spells/01_inject.txt")
        val properties = PsiTreeUtil.findChildrenOfType(psiFile, ParadoxScriptProperty::class.java)
        val injectShared = properties.single { it.name == "INJECT:shared_name" }
        val injectMists = properties.single { it.name == "INJECT:spell_of_mists" }
        val expectedInfos = listOf(
            ParadoxDefinitionInjectionIndexInfo("INJECT", "shared_name", "academy_spell", injectShared.startOffset, gameType),
            ParadoxDefinitionInjectionIndexInfo("INJECT", "spell_of_mists", "academy_spell", injectMists.startOffset, gameType),
        )

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        Assert.assertEquals(expectedInfos, fileData[PlsIndexUtil.createAllKey()])
        Assert.assertEquals(expectedInfos, fileData[PlsIndexUtil.createTypeKey("academy_spell")])
        Assert.assertEquals(listOf(expectedInfos[0]), fileData[PlsIndexUtil.createNameTypeKey("shared_name", "academy_spell")])
    }

    // endregion

    // region Cross-File Aggregation

    @Test
    fun testDefinitionInjectionIndex_CrossFileAggregation_ByTarget() {
        // Arrange: 同一 target 出现在不同类型的文件中
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")
        markFileInfo(gameType, "common/academy_spells/01_inject.txt")
        myFixture.configureByFile("features/index/common/academy_spells/01_inject.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val sharedInfos = FileBasedIndex.getInstance()
            .getValues(PlsIndexKeys.DefinitionInjection, PlsIndexUtil.createNameKey("shared_name"), scope).flatten()

        // Assert: "shared_name" 出现在 arcane_tome（TRY_INJECT）和 academy_spell（INJECT）两种类型中
        Assert.assertEquals(2, sharedInfos.size)
        Assert.assertEquals(setOf("arcane_tome", "academy_spell"), sharedInfos.map { it.type }.toSet())
    }

    @Test
    fun testDefinitionInjectionIndex_CrossFileAggregation_ByType() {
        // Arrange: 跨文件按类型聚合
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")
        markFileInfo(gameType, "common/academy_spells/01_inject.txt")
        myFixture.configureByFile("features/index/common/academy_spells/01_inject.txt")

        // Act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val allInfos = FileBasedIndex.getInstance().getValues(PlsIndexKeys.DefinitionInjection, PlsIndexUtil.createAllKey(), scope).flatten()

        // Assert: arcane_tome(4) + academy_spell(2) = 6
        Assert.assertEquals(6, allInfos.size)
        Assert.assertEquals(setOf("arcane_tome", "academy_spell"), allInfos.map { it.type }.toSet())
    }

    // endregion

    // region Edge Cases

    @Test
    fun testDefinitionInjectionIndex_NoMatchedTypeConfig() {
        // Arrange: 路径无匹配的类型规则
        markFileInfo(gameType, "common/no_rule/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/no_rule/01_inject.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
    }

    @Test
    fun testDefinitionInjectionIndex_WrongExtension() {
        // Arrange: 扩展名不匹配（规则要求 .dat，实际为 .txt）
        markFileInfo(gameType, "common/forbidden/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/forbidden/01_inject.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
    }

    @Test
    fun testDefinitionInjectionIndex_EmptyTarget_Ignored() {
        // Arrange: target 为空的注入表达式（如 "INJECT: = {}"）
        markFileInfo(gameType, "common/arcane_tomes/02_edge.txt")
        val psiFile = myFixture.configureByFile("features/index/common/arcane_tomes/02_edge.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
    }

    @Test
    fun testDefinitionInjectionIndex_ElementOffset() {
        // Arrange: 验证 elementOffset 互不相同且有序
        markFileInfo(gameType, "common/arcane_tomes/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        val allInfos = fileData[PlsIndexUtil.createAllKey()].orEmpty()
        val offsets = allInfos.map { it.elementOffset }
        Assert.assertEquals(4, offsets.size)
        Assert.assertEquals(offsets.toSet().size, offsets.size) // 互不相同
        Assert.assertTrue(offsets.all { it >= 0 })
    }

    // endregion
}

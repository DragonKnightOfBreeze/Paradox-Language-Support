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

    private fun Collection<ParadoxDefinitionInjectionIndexInfo>.sorted(): List<ParadoxDefinitionInjectionIndexInfo> {
        return sortedWith(compareBy({ it.type }, { it.target }, { it.mode }))
    }

    @Test
    fun testDefinitionInjectionIndex_Basic() {
        // https://github.com/DragonKnightOfBreeze/cwtools-vic3-config/blob/master/config/common/ai_strategies.cwt

        markFileInfo(ParadoxGameType.Vic3, "common/ai_strategies/00_default.txt")
        myFixture.configureByFile("features/index/common/ai_strategies/00_default.txt")

        markFileInfo(ParadoxGameType.Vic3, "common/ai_strategies/01_inject.txt")
        myFixture.configureByFile("features/index/common/ai_strategies/01_inject.txt")
        
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val allData = FileBasedIndex.getInstance().getValues(PlsIndexKeys.DefinitionInjection, PlsIndexUtil.createAllKey(), scope).flatten()
        val expect = listOf(
            ParadoxDefinitionInjectionIndexInfo("INJECT", "ai_strategy_default", "ai_strategy", 0, ParadoxGameType.Vic3)
        )
        Assert.assertEquals(expect, allData)
    }

    @Test
    fun testDefinitionInjectionIndex_MultipleModesAndIgnoredCases() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/arcane_tomes/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/arcane_tomes/01_inject.txt")

        val properties = PsiTreeUtil.findChildrenOfType(psiFile, ParadoxScriptProperty::class.java)
        val injectFlames = properties.single { it.name == "INJECT:tome_of_flames" }
        val replaceIce = properties.single { it.name == "REPLACE:tome_of_ice" }
        val tryInjectShared = properties.single { it.name == "TRY_INJECT:shared_name" }

        val expectedInfos = listOf(
            ParadoxDefinitionInjectionIndexInfo("INJECT", "tome_of_flames", "arcane_tome", injectFlames.startOffset, ParadoxGameType.Vic3),
            ParadoxDefinitionInjectionIndexInfo("REPLACE", "tome_of_ice", "arcane_tome", replaceIce.startOffset, ParadoxGameType.Vic3),
            ParadoxDefinitionInjectionIndexInfo("TRY_INJECT", "shared_name", "arcane_tome", tryInjectShared.startOffset, ParadoxGameType.Vic3),
        )

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert: key 分发
        val allKey = PlsIndexUtil.createAllKey()
        val typeKey = PlsIndexUtil.createTypeKey("arcane_tome")
        val nameKey1 = PlsIndexUtil.createNameKey("tome_of_flames")
        val nameTypeKey1 = PlsIndexUtil.createNameTypeKey("tome_of_flames", "arcane_tome")

        Assert.assertEquals(expectedInfos.sorted(), fileData[allKey].orEmpty().sorted())
        Assert.assertEquals(expectedInfos.sorted(), fileData[typeKey].orEmpty().sorted())
        Assert.assertEquals(listOf(expectedInfos[0]), fileData[nameKey1])
        Assert.assertEquals(listOf(expectedInfos[0]), fileData[nameTypeKey1])

        // Assert: 非法写法应被忽略
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("tome_scalar")])
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("should_be_ignored")])
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("A_\$PARAM\$_B")])
        Assert.assertNull(fileData[PlsIndexUtil.createNameKey("nested_should_be_ignored")])
    }

    @Test
    fun testDefinitionInjectionIndex_DifferentTypes_ByFilePath() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/academy_spells/01_inject.txt")
        val psiFile = myFixture.configureByFile("features/index/common/academy_spells/01_inject.txt")
        val properties = PsiTreeUtil.findChildrenOfType(psiFile, ParadoxScriptProperty::class.java)
        val injectShared = properties.single { it.name == "INJECT:shared_name" }
        val injectMists = properties.single { it.name == "INJECT:spell_of_mists" }
        val expectedInfos = listOf(
            ParadoxDefinitionInjectionIndexInfo("INJECT", "shared_name", "academy_spell", injectShared.startOffset, ParadoxGameType.Vic3),
            ParadoxDefinitionInjectionIndexInfo("INJECT", "spell_of_mists", "academy_spell", injectMists.startOffset, ParadoxGameType.Vic3),
        )

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        Assert.assertEquals(expectedInfos, fileData[PlsIndexUtil.createAllKey()])
        Assert.assertEquals(expectedInfos, fileData[PlsIndexUtil.createTypeKey("academy_spell")])
        Assert.assertEquals(listOf(expectedInfos[0]), fileData[PlsIndexUtil.createNameTypeKey("shared_name", "academy_spell")])
    }

    @Test
    fun testDefinitionInjectionIndex_NoMatchedTypeConfig() {
        // Arrange
        run {
            markFileInfo(ParadoxGameType.Vic3, "common/no_rule/01_inject.txt")
            val psiFile = myFixture.configureByFile("features/index/common/no_rule/01_inject.txt")

            // Act
            val project = project
            val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

            // Assert
            Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
        }
        run {
            markFileInfo(ParadoxGameType.Vic3, "common/forbidden/01_inject.txt")
            val psiFile = myFixture.configureByFile("features/index/common/forbidden/01_inject.txt")

            val project = project
            val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

            Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
        }
    }

    @Test
    fun testDefinitionInjectionIndex_EmptyTarget_Ignored() {
        // Arrange
        markFileInfo(ParadoxGameType.Vic3, "common/arcane_tomes/02_edge.txt")
        val psiFile = myFixture.configureByFile("features/index/common/arcane_tomes/02_edge.txt")

        // Act
        val project = project
        val fileData = FileBasedIndex.getInstance().getFileData(PlsIndexKeys.DefinitionInjection, psiFile.virtualFile, project)

        // Assert
        Assert.assertTrue("fileData=$fileData", fileData.isEmpty())
    }
}

package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.search.ParadoxParameterSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.parameter
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptProperty
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxInlineScriptIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testUsageIndex_DirectForm() {
        myFixture.configureByFile("features/index/usage_direct_stellaris.test.txt")
        injectFileInfo("common/test/usage_direct_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptUsage,
            "test_inline",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        val p = elements.single()
        Assert.assertEquals("inline_script", p.name)
    }

    fun testUsageIndex_BlockForm() {
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        injectFileInfo("common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptUsage,
            "test_inline",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        Assert.assertEquals(1, elements.size)
        val p = elements.single()
        Assert.assertEquals("inline_script", p.name)
    }

    fun testArgumentIndex_BlockForm_All() {
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        injectFileInfo("common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptArgument,
            "test_inline",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        val names = elements.map { it.name }.sorted()
        Assert.assertEquals(listOf("EVENT_ID", "SOME_FLAG"), names)
    }

    fun testUsageIndex_Parameterized_ShouldSkip() {
        myFixture.configureByFile("features/index/usage_parameterized_stellaris.test.txt")
        injectFileInfo("common/test/usage_parameterized_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptUsage,
            "test_\$PARAM$",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        Assert.assertEquals(0, elements.size)
        val arguments = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptArgument,
            "test_\$PARAM$",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        Assert.assertEquals(0, arguments.size)
    }

    fun testUsageIndex_VariableRef_ShouldSkip() {
        myFixture.configureByFile("features/index/usage_variable_ref_stellaris.test.txt")
        injectFileInfo("common/test/usage_variable_ref_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptUsage,
            "test_inline",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        Assert.assertEquals(0, elements.size)
        val arguments = StubIndex.getElements(
            ParadoxIndexKeys.InlineScriptArgument,
            "test_inline",
            project,
            scope,
            ParadoxScriptProperty::class.java
        )
        Assert.assertEquals(0, arguments.size)
    }

    fun testParameterSearcher_All() {
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        injectFileInfo("common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file).parameter()
        val contextKey = "inline_script@test_inline"
        val results = mutableListOf<String>()
        ParadoxParameterSearch.search(null, contextKey, selector).processQuery(false) { info ->
            results += info.name
            true
        }
        Assert.assertEquals(setOf("EVENT_ID", "SOME_FLAG"), results.toSet())
    }

    fun testParameterSearcher_ByName() {
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        injectFileInfo("common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, myFixture.file).parameter()
        val contextKey = "inline_script@test_inline"
        val results = mutableListOf<String>()
        ParadoxParameterSearch.search("EVENT_ID", contextKey, selector).processQuery(false) { info ->
            results += info.name
            true
        }
        Assert.assertEquals(listOf("EVENT_ID"), results)
    }

    private fun injectFileInfo(relPath: String, gameType: ParadoxGameType) {
        val vFile = myFixture.file.virtualFile
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(relPath), "", ParadoxFileType.Script, ParadoxRootInfo.Injected(gameType))
        vFile.putUserData(PlsKeys.injectedFileInfo, fileInfo)
        vFile.putUserData(PlsKeys.injectedGameType, gameType)
    }
}

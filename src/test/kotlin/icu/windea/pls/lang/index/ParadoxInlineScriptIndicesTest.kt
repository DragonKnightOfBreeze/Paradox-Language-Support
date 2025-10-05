package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxParameterSearch
import icu.windea.pls.lang.search.processQuery
import icu.windea.pls.lang.search.selector.parameter
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.PlsTestUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxInlineScriptIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun usageIndex_DirectForm() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_direct_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_direct_stellaris.test.txt", ParadoxGameType.Stellaris)
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

    @Test
    fun usageIndex_BlockForm() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
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

    @Test
    fun argumentIndex_BlockForm_All() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
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

    @Test
    fun usageIndex_Parameterized_ShouldSkip() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_parameterized_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_parameterized_stellaris.test.txt", ParadoxGameType.Stellaris)
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

    @Test
    fun usageIndex_VariableRef_ShouldSkip() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_variable_ref_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_variable_ref_stellaris.test.txt", ParadoxGameType.Stellaris)
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

    @Test
    fun parameterSearcher_All() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).parameter()
        val contextKey = "inline_script@test_inline"
        val results = mutableListOf<String>()
        ParadoxParameterSearch.search(null, contextKey, selector).processQuery(false) { info ->
            results += info.name
            true
        }
        Assert.assertEquals(setOf("EVENT_ID", "SOME_FLAG"), results.toSet())
    }

    @Test
    fun parameterSearcher_ByName() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).parameter()
        val contextKey = "inline_script@test_inline"
        val results = mutableListOf<String>()
        ParadoxParameterSearch.search("EVENT_ID", contextKey, selector).processQuery(false) { info ->
            results += info.name
            true
        }
        Assert.assertEquals(listOf("EVENT_ID"), results)
    }

    @Test
    fun parameterSearcher_ByName_NotFound() {
        val file = myFixture.file.virtualFile
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        PlsTestUtil.injectFileInfo(file, "common/test/usage_block_stellaris.test.txt", ParadoxGameType.Stellaris)
        val project = project
        val selector = selector(project, file).parameter()
        val contextKey = "inline_script@test_inline"
        val results = mutableListOf<String>()
        ParadoxParameterSearch.search("NOT_EXISTS", contextKey, selector).processQuery(false) { info ->
            results += info.name
            true
        }
        Assert.assertTrue(results.isEmpty())
    }
}

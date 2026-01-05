package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxInlineScriptIndicesTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        initConfigGroups(project, ParadoxGameType.Stellaris) // required (depends on `directive[inline_script]` configs)
        markIntegrationTest()
    }

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testUsageIndex_DirectForm() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_direct_stellaris.test.txt")
        myFixture.configureByFile("features/index/usage_direct_stellaris.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(PlsIndexKeys.InlineScriptUsage, "test_inline", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, elements.size)
        val p = elements.single()
        Assert.assertEquals("inline_script", p.name)
    }

    @Test
    fun testUsageIndex_BlockForm() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_block_stellaris.test.txt")
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(PlsIndexKeys.InlineScriptUsage, "test_inline", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, elements.size)
        val p = elements.single()
        Assert.assertEquals("inline_script", p.name)
    }

    @Test
    fun testArgumentIndex_BlockForm_All() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_block_stellaris.test.txt")
        myFixture.configureByFile("features/index/usage_block_stellaris.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(PlsIndexKeys.InlineScriptArgument, "test_inline", project, scope, ParadoxScriptProperty::class.java)
        val names = elements.map { it.name }.sorted()
        Assert.assertEquals(listOf("EVENT_ID", "SOME_FLAG"), names)
    }

    @Test
    fun testUsageIndex_Parameterized_ShouldSkip() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_parameterized_stellaris.test.txt")
        myFixture.configureByFile("features/index/usage_parameterized_stellaris.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(PlsIndexKeys.InlineScriptUsage, "test_\$PARAM$", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(0, elements.size)
        val arguments = StubIndex.getElements(PlsIndexKeys.InlineScriptArgument, "test_\$PARAM$", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(0, arguments.size)
    }

    @Test
    fun testUsageIndex_VariableRef_ShouldSkip() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_variable_ref_stellaris.test.txt")
        myFixture.configureByFile("features/index/usage_variable_ref_stellaris.test.txt")
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val elements = StubIndex.getElements(PlsIndexKeys.InlineScriptUsage, "test_inline", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(0, elements.size)
        val arguments = StubIndex.getElements(PlsIndexKeys.InlineScriptArgument, "test_inline", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(0, arguments.size)
    }
}

package icu.windea.pls.lang.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.clearIntegrationTest
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
class ParadoxDefineIndexTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testDefineIndex_Basic() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/defines_basic_stellaris.test.txt")
        myFixture.configureByFile("features/index/common/defines/defines_basic_stellaris.test.txt")

        // act
        val project = project
        val scope = GlobalSearchScope.projectScope(project)
        val namespaces = StubIndex.getElements(PlsIndexKeys.DefineNamespace, "NGameplay", project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, namespaces.size)
        Assert.assertEquals("NGameplay", namespaces.single().name)

        val marineKey = "NGameplay\u0000MARINE"
        val variables = StubIndex.getElements(PlsIndexKeys.DefineVariable, marineKey, project, scope, ParadoxScriptProperty::class.java)
        Assert.assertEquals(1, variables.size)
        Assert.assertEquals("MARINE", variables.single().name)
    }

    @Test
    fun testDefineSearcher_ByNamespaceAndVariable() {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/defines_basic_stellaris.test.txt")
        myFixture.configureByFile("features/index/common/defines/defines_basic_stellaris.test.txt")

        // act
        val selector = selector(project, myFixture.file).define()
        val results = mutableListOf<ParadoxScriptProperty>()
        ParadoxDefineSearch.search("NGameplay", "MARINE", selector).process { element ->
            results += element
            true
        }

        // assert
        Assert.assertEquals("results=$results", 1, results.size)
        val info = results.single().defineInfo
        Assert.assertNotNull(info)
        info!!
        Assert.assertEquals("NGameplay", info.namespace)
        Assert.assertEquals("MARINE", info.variable)
    }
}

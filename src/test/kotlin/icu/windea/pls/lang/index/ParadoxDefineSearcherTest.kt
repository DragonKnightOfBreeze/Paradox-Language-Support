package icu.windea.pls.lang.index

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
class ParadoxDefineSearcherTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    private fun configureDefineFile(testDataPath: String) {
        markFileInfo(ParadoxGameType.Stellaris, "common/defines/${testDataPath.substringAfterLast('/')}")
        myFixture.configureByFile(testDataPath)
    }

    private fun assertNoDefineInfo(elements: Collection<ParadoxScriptProperty>) {
        val infos = elements.mapNotNull { it.defineInfo }
        Assert.assertTrue("infos=$infos", infos.isEmpty())
    }

    @Test
    fun testDefineSearcher_ByNamespaceAndVariable() {
        configureDefineFile("features/index/common/defines/defines_basic_stellaris.test.txt")

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

    @Test
    fun testDefineSearcher_ByNamespaceOnly() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineSearch.search("NGameplay", "", selector).findAll()
            .mapNotNull { it.defineInfo }

        Assert.assertEquals(1, results.size)
        val info = results.single()
        Assert.assertEquals("NGameplay", info.namespace)
        Assert.assertNull(info.variable)
    }

    @Test
    fun testDefineSearcher_ByNamespace_AllVariables() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineSearch.search("NGameplay", null, selector).findAll()
            .mapNotNull { it.defineInfo }
            .sortedBy { it.expression }

        Assert.assertEquals(3, results.size)
        Assert.assertEquals(listOf("NGameplay", "NGameplay.FLEET_POWER", "NGameplay.MARINE"), results.map { it.expression })
    }

    @Test
    fun testDefineSearcher_AllNamespaces() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineSearch.search(null, "", selector).findAll()
            .mapNotNull { it.defineInfo }
            .sortedBy { it.namespace }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(listOf("NEconomy", "NGameplay"), results.map { it.namespace })
        Assert.assertTrue(results.all { it.variable == null })
    }

    @Test
    fun testDefineSearcher_ByVariableAcrossNamespaces() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineSearch.search(null, "MARINE", selector).findAll()
            .mapNotNull { it.defineInfo }
            .sortedBy { it.namespace }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(listOf("NEconomy.MARINE", "NGameplay.MARINE"), results.map { it.expression })
    }

    @Test
    fun testDefineSearcher_AllDefines() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineSearch.search(null, null, selector).findAll()
            .mapNotNull { it.defineInfo }
            .sortedBy { it.expression }

        Assert.assertEquals(7, results.size)
        Assert.assertFalse(results.any { it.namespace == "NOT_A_DEFINE" })
        Assert.assertFalse(results.any { it.variable == "NOT_A_DEFINE" })
    }

    @Test
    fun testDefineSearcher_Edge_AllDefines() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineSearch.search(null, null, selector).findAll()

        val infos = elements.mapNotNull { it.defineInfo }.sortedBy { it.expression }
        Assert.assertEquals(4, infos.size)
        Assert.assertEquals(listOf("NGameplay", "NGameplay.FLEET_POWER", "NGameplay.MARINE", "NGameplay.NESTED"), infos.map { it.expression })
    }

    @Test
    fun testDefineSearcher_Edge_ByNamespace_AllVariables() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineSearch.search("NGameplay", null, selector).findAll()
        val infos = elements.mapNotNull { it.defineInfo }.sortedBy { it.expression }

        Assert.assertEquals(4, infos.size)
        Assert.assertEquals(listOf("NGameplay", "NGameplay.FLEET_POWER", "NGameplay.MARINE", "NGameplay.NESTED"), infos.map { it.expression })
    }

    @Test
    fun testDefineSearcher_Edge_ByNamespaceAndVariable_ParameterizedVariable() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineSearch.search("NGameplay", "A_\$PARAM\$_B", selector).findAll()
        assertNoDefineInfo(elements)
    }

    @Test
    fun testDefineSearcher_Edge_ByNamespaceAndVariable_NestedVariable() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineSearch.search("NGameplay", "INSIDE", selector).findAll()
        assertNoDefineInfo(elements)
    }
}

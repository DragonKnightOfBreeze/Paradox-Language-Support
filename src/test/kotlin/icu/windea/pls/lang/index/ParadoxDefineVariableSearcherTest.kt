package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.process
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
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
class ParadoxDefineVariableSearcherTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun configureDefineFile(@TestDataFile testDataPath: String) {
        markFileInfo(gameType, "common/defines/${testDataPath.substringAfterLast('/')}")
        myFixture.configureByFile(testDataPath)
    }

    private fun assertNoDefineVariableInfo(elements: Collection<ParadoxScriptProperty>) {
        val infos = elements.mapNotNull { it.defineVariableInfo }
        Assert.assertTrue("infos=$infos", infos.isEmpty())
    }

    // region Search By Namespace And Variable

    @Test
    fun byNamespaceAndVariable() {
        configureDefineFile("features/index/common/defines/defines_basic_stellaris.test.txt")

        // act
        val selector = selector(project, myFixture.file).define()
        val results = mutableListOf<ParadoxScriptProperty>()
        ParadoxDefineVariableSearch.search("NGameplay", "MARINE", selector).process { element ->
            results += element
            true
        }

        // assert
        Assert.assertEquals("results=$results", 1, results.size)
        val info = results.single().defineVariableInfo
        Assert.assertNotNull(info)
        info!!
        Assert.assertEquals("NGameplay", info.namespace)
        Assert.assertEquals("MARINE", info.variable)
    }

    // endregion

    // region Search By Namespace

    @Test
    fun byNamespace_AllVariables() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineVariableSearch.search("NGameplay", null, selector).findAll()
            .mapNotNull { it.defineVariableInfo }
            .sortedBy { it.expression }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(listOf("NGameplay.FLEET_POWER", "NGameplay.MARINE"), results.map { it.expression })
    }

    // endregion

    // region Search By Variable

    @Test
    fun byVariableAcrossNamespaces() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineVariableSearch.search(null, "MARINE", selector).findAll()
            .mapNotNull { it.defineVariableInfo }
            .sortedBy { it.namespace }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(listOf("NEconomy.MARINE", "NGameplay.MARINE"), results.map { it.expression })
    }

    // endregion

    // region Edge Cases

    @Test
    fun edge_ByNamespace_AllVariables() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineVariableSearch.search("NGameplay", null, selector).findAll()
        val infos = elements.mapNotNull { it.defineVariableInfo }.sortedBy { it.expression }

        Assert.assertEquals(3, infos.size)
        Assert.assertEquals(listOf("NGameplay.FLEET_POWER", "NGameplay.MARINE", "NGameplay.NESTED"), infos.map { it.expression })
    }

    @Test
    fun edge_byNamespaceAndVariable_parameterizedVariable() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineVariableSearch.search("NGameplay", "A_\$PARAM\$_B", selector).findAll()
        assertNoDefineVariableInfo(elements)
    }

    @Test
    fun edge_byNamespaceAndVariable_NestedVariable() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineVariableSearch.search("NGameplay", "INSIDE", selector).findAll()
        assertNoDefineVariableInfo(elements)
    }

    // endregion
}

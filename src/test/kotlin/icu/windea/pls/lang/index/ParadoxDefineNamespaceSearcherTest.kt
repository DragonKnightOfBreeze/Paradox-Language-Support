package icu.windea.pls.lang.index

import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.defineNamespaceInfo
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
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
class ParadoxDefineNamespaceSearcherTest : BasePlatformTestCase() {
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

    private fun assertNoDefineNamespaceInfo(elements: Collection<ParadoxScriptProperty>) {
        val infos = elements.mapNotNull { it.defineNamespaceInfo }
        Assert.assertTrue("infos=$infos", infos.isEmpty())
    }

    // region Search By Namespace

    @Test
    fun byNamespaceOnly() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineNamespaceSearch.search("NGameplay", selector).findAll()
            .mapNotNull { it.defineNamespaceInfo }

        Assert.assertEquals(1, results.size)
        val info = results.single()
        Assert.assertEquals("NGameplay", info.namespace)
    }

    @Test
    fun allNamespaces() {
        configureDefineFile("features/index/common/defines/defines_complex_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val results = ParadoxDefineNamespaceSearch.search(null, selector).findAll()
            .mapNotNull { it.defineNamespaceInfo }
            .sortedBy { it.namespace }

        Assert.assertEquals(2, results.size)
        Assert.assertEquals(listOf("NEconomy", "NGameplay"), results.map { it.namespace })
    }

    // endregion

    // region Edge Cases

    @Test
    fun edge_allNamespaces() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineNamespaceSearch.search(null, selector).findAll()

        val infos = elements.mapNotNull { it.defineNamespaceInfo }.sortedBy { it.expression }
        Assert.assertEquals(1, infos.size)
        Assert.assertEquals(listOf("NGameplay"), infos.map { it.expression })
    }

    @Test
    fun edge_notBlockValueNamespace() {
        configureDefineFile("features/index/common/defines/defines_edge_stellaris.test.txt")

        val selector = selector(project, myFixture.file).define()
        val elements = ParadoxDefineNamespaceSearch.search("N_NOT_BLOCK", selector).findAll()
        assertNoDefineNamespaceInfo(elements)
    }

    // endregion
}

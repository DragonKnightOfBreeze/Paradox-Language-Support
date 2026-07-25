package icu.windea.pls.lang.index

import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.util.withSearchScope
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxDynamicValueSearch
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxDynamicValueSearchTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/index")
        markConfigDirectory("features/index/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region Grimoires

    @Test
    fun testGrimoires_InDefinition() {
        configureMarkedFile("features/index/common/grimoires/00_grimoires.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val selector = ParadoxDynamicValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDynamicValueSearch.search("spirit_art", "grimoire_tag", selector).findAll()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("spirit_art", results.single().name)
        Assert.assertEquals("grimoire_tag", results.single().type)
    }

    @Test
    fun testGrimoires_InDefinition_FindAll() {
        configureMarkedFile("features/index/common/grimoires/00_grimoires.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val selector = ParadoxDynamicValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDynamicValueSearch.search(null, "grimoire_tag", selector).findAll()

        val magicNames = results.map { it.name }.toSet()
        assertNotEmpty(magicNames)

        val expected = setOf(
            "spell",
            "spell_special",
            "spirit_art",
        )
        Assert.assertEquals(expected, magicNames)
    }

    @Test
    fun testGrimoires_FromColumn() {
        configureMarkedFile("features/index/common/grimoire_tocs/00_grimoire_tocs.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val selector = ParadoxDynamicValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDynamicValueSearch.search("arms_processed", "magic_tag", selector).findAll()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("arms_processed", results.single().name)
        Assert.assertEquals("magic_tag", results.single().type)
    }

    @Test
    fun testGrimoires_FromColumn_FindAll() {
        configureMarkedFile("features/index/common/grimoire_tocs/00_grimoire_tocs.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val selector = ParadoxDynamicValueSearch.selector(project, myFixture.file).withSearchScope(GlobalSearchScope.projectScope(project))
        val results = ParadoxDynamicValueSearch.search(null, "magic_tag", selector).findAll()

        val magicNames = results.map { it.name }.toSet()
        assertNotEmpty(magicNames)

        val expected = setOf(
            "elemental",
            "elemental_protection",
            "arms_processed",
            "shining",
            "shining_protection",
            "shining_special",
        )
        Assert.assertEquals(expected, magicNames)
    }

    // endregion

    private fun configureMarkedFile(@TestDataFile testDataPath: String, path: String = testDataPath.removePrefix("features/index/")): PsiFile {
        markFileInfo(gameType, path)
        return myFixture.configureByFile(testDataPath)
    }
}

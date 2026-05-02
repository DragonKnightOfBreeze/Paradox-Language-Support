package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.codeInsight.annotated.ParadoxAnnotatedLevel
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
import icu.windea.pls.model.ParadoxGameType

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvTextAnnotatedRendererTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private val gameType = ParadoxGameType.Stellaris

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/renderers")
        markConfigDirectory("features/renderers/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun example() {
        configureFile("common/misc/example.test.csv")
        assertResult("common/misc/example.test.csv", ParadoxAnnotatedLevel.BASIC)
    }

    @Test
    fun example_unformatted() {
        configureFile("common/misc/example_unformatted.test.csv")
        assertResult("common/misc/example_unformatted.test.csv", ParadoxAnnotatedLevel.BASIC)
    }

    @Test
    fun chronicle() {
        configureFile("common/chapters/00_chapters.txt")
        configureFile("common/chapters/categories/00_chapter_categories.txt")
        configureFile("common/chapters/00_chapter_pages.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertResult("common/chapters/00_chapter_pages.csv", ParadoxAnnotatedLevel.ALL)
    }

    private fun configureFile(path: String) {
        markFileInfo(gameType, path)
        myFixture.copyFileToProject("features/renderers/$path", path)
    }

    private fun assertResult(path: String, level: ParadoxAnnotatedLevel) {
        val file = myFixture.configureFromTempProjectFile(path)
        file as ParadoxCsvFile
        val renderer = ParadoxCsvTextAnnotatedRenderer().apply { settings.level = level }
        val result = renderer.render(file)
        val annotatedPath = path.substringBeforeLast('.') + ".annotated." + path.substringAfterLast('.')
        val annotatedFile = myFixture.configureByFile("features/renderers/$annotatedPath")
        annotatedFile as ParadoxCsvFile
        Assert.assertEquals(annotatedFile.text.trimEnd(), result.trimEnd())
    }
}

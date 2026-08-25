package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.data.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxCsvTextAnnotatedRenderer
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvTextAnnotatedRendererTest : BasePlatformTestCase(), ChronicleTestScope {
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
    fun smokeTest_example() {
        configureMarkedFile("features/renderers/common/misc/example.test.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertResult("features/renderers/common/misc/example.test.csv", ParadoxAnnotatedLevel.BASIC)
    }

    @Test
    fun smokeTest_example_unformatted() {
        configureMarkedFile("features/renderers/common/misc/example_unformatted.test.csv")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertResult("features/renderers/common/misc/example_unformatted.test.csv", ParadoxAnnotatedLevel.BASIC)
    }

    private fun configureMarkedFile(@TestDataFile testDataPath: String, path: String = testDataPath.removePrefix("features/renderers/")): String {
        markFileInfo(gameType, path)
        myFixture.configureByFile(testDataPath)
        return testDataPath
    }

    @Suppress("SameParameterValue")
    private fun assertResult(@TestDataFile testDataPath: String, level: ParadoxAnnotatedLevel) {
        val file = myFixture.configureFromTempProjectFile(testDataPath)
        file as ParadoxCsvFile
        val renderer = ParadoxCsvTextAnnotatedRenderer().apply { settings.level = level }
        val result = renderer.render(file)
        val annotatedTestDataPath = testDataPath.substringBeforeLast('.') + ".annotated." + testDataPath.substringAfterLast('.')
        val annotatedFile = myFixture.configureByFile(annotatedTestDataPath)
        annotatedFile as ParadoxCsvFile
        Assert.assertEquals(annotatedFile.text.trimEnd(), result.trimEnd())
    }
}

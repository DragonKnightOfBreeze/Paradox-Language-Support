package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.codeInsight.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
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

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptTextAnnotatedRendererTest : BasePlatformTestCase() {
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
        configureFile("common/misc/example.test.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertResult("common/misc/example.test.txt", ParadoxAnnotatedLevel.BASIC)
    }

    @Test
    fun smokeTest_example_unformatted() {
        configureFile("common/misc/example_unformatted.test.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        assertResult("common/misc/example_unformatted.test.txt", ParadoxAnnotatedLevel.BASIC)
    }

    private fun configureFile(path: String) {
        markFileInfo(gameType, path)
        myFixture.copyFileToProject("features/renderers/$path", path)
    }

    @Suppress("SameParameterValue")
    private fun assertResult(path: String, level: ParadoxAnnotatedLevel) {
        val file = myFixture.configureFromTempProjectFile(path)
        file as ParadoxScriptFile
        val renderer = ParadoxScriptTextAnnotatedRenderer().apply { settings.level = level }
        val result = renderer.render(file)
        val annotatedPath = path.substringBeforeLast('.') + ".annotated." + path.substringAfterLast('.')
        val annotatedFile = myFixture.configureByFile("features/renderers/$annotatedPath")
        annotatedFile as ParadoxScriptFile
        Assert.assertEquals(annotatedFile.text.trimEnd(), result.trimEnd())
    }
}

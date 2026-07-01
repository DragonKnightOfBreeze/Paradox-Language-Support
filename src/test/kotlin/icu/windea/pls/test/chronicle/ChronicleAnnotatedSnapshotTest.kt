package icu.windea.pls.test.chronicle

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.codeInsight.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.renderers.ParadoxCsvTextAnnotatedRenderer
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextAnnotatedRenderer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markConfigDirectory
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path

/**
 * 基于数种注解渲染器的快照测试。
 *
 * @see ParadoxScriptTextAnnotatedRenderer
 * @see ParadoxCsvTextAnnotatedRenderer
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ChronicleAnnotatedSnapshotTest : ChronicleSnapshotTest() {
    private val gameType = ParadoxGameType.Stellaris

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("chronicle")
        markConfigDirectory("chronicle/.config")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun test() {
        val dataFilePaths = getDataFiles()
        val files = configureDataFiles(dataFilePaths)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        annotateDataFiles(files)
    }

    private fun getDataFiles(): List<Path> {
        val dataFilePaths = computeDataFilePaths()
        assertNotEmpty(dataFilePaths)
        println("Number of data files: ${dataFilePaths.size}")
        dataFilePaths.forEach { println("- ${it.toString().normalizePath()}") }
        return dataFilePaths
    }

    private fun configureDataFiles(dataFilePaths: List<Path>): MutableList<VirtualFile> {
        val files = mutableListOf<VirtualFile>()
        for (dataFilePath in dataFilePaths) {
            val filePath = dataFilePath.toString().normalizePath()
            val markedPath = filePath.removePrefix("chronicle/")
            markFileInfo(gameType, markedPath)
            files += myFixture.configureByFile(filePath).virtualFile
        }
        return files
    }

    private fun annotateDataFiles(files: MutableList<VirtualFile>) {
        val annotatedLevel = ParadoxAnnotatedLevel.ALL
        for (file in files) {
            val result = render(file, annotatedLevel) ?: continue
            val path = file.fileInfo?.path?.path ?: throw IllegalStateException()
            val annotatedPath = path.substringBeforeLast('.') + ".annotated." + path.substringAfterLast('.')
            val annotatedFile = myFixture.configureByFile("chronicle/.annotated/$annotatedPath")
            println("Assert annotated data file: ${annotatedPath}")
            assertEquals(annotatedFile.text.trimEnd(), result.trimEnd())
        }
    }

    private fun render(file: VirtualFile, annotatedLevel: ParadoxAnnotatedLevel): String? {
        val psiFile = file.toPsiFile(project)
        return when (psiFile) {
            is ParadoxScriptFile -> {
                val renderer = ParadoxScriptTextAnnotatedRenderer().apply { settings.level = annotatedLevel }
                renderer.render(psiFile)
            }
            is ParadoxCsvFile -> {
                val renderer = ParadoxCsvTextAnnotatedRenderer().apply { settings.level = annotatedLevel }
                renderer.render(psiFile)
            }
            else -> null
        }
    }
}

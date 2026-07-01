package icu.windea.pls.test.chronicle

import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toClass
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ChronicleConstants
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
 * 基于各种默认启用的代码检查的快照测试。
 *
 * 目前不检查存在警告和错误的情况。
 *
 * @see LocalInspectionTool
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ChronicleInspectionsSnapshotTest : ChronicleSnapshotTest() {
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
        highlightDataFiles(files)
    }

    private fun getDataFiles(): List<Path> {
        val dataFilePaths = computeDataFilePaths()
        assertNotEmpty(dataFilePaths)
        println("Number of data files: ${dataFilePaths.size}")
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

    private fun highlightDataFiles(files: MutableList<VirtualFile>) {
        myFixture.enableInspections(*getEnabledInspections().toTypedArray())
        myFixture.testHighlightingAllFiles(true, false, true, *files.toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    private fun getEnabledInspections(): List<Class<out LocalInspectionTool>> {
        val types = mutableListOf<Class<out LocalInspectionTool>>()
        for (ep in LocalInspectionEP.LOCAL_INSPECTION.extensionList) {
            if (ep.pluginDescriptor.pluginId != ChronicleConstants.pluginId) continue
            if (!ep.enabledByDefault) continue
            val type = ep.implementationClass.toClass() as Class<out LocalInspectionTool>
            types += type
        }
        println("Number of enabled inspections: ${types.size}")
        return types
    }
}

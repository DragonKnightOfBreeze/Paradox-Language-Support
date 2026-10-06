package icu.windea.pls.test.chronicle

import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.core.toClass
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ChronicleConstants
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * 基于各种默认启用的代码检查的快照测试。
 *
 * 执行逻辑：
 * 1. 收集并配置 [chroniclePath] 下的所有测试数据文件，同时把它们拷贝到输出目录用于差异比较。
 * 2. 启用本插件所有默认启用的 [LocalInspectionTool]。
 * 3. 对每个数据文件单独执行高亮检查，记录其通过与否，并输出逐文件状态与整体汇总。
 *
 * 目前只检查不存在警告和错误的情况（即期望检查结果为空）。
 *
 * @see ChronicleSnapshotTest
 * @see LocalInspectionTool
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ChronicleInspectionBasedSnapshotTest : ChronicleSnapshotTest() {
    override val gameType = ParadoxGameType.Stellaris

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
        logHeader("Inspection-based snapshot test")
        val dataFilePaths = getDataFiles()
        val snapshotFiles = configureDataFiles(dataFilePaths)

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        copyDataFiles(dataFilePaths)

        highlightDataFiles(snapshotFiles)
    }

    private fun highlightDataFiles(snapshotFiles: List<SnapshotDataFile>) {
        val inspections = getEnabledInspections()
        myFixture.enableInspections(*inspections.toTypedArray())
        log("Number of enabled inspections: ${inspections.size}")
        inspections.forEach { inspection -> log("- ${inspection.name}") }
        log("Expected result: no warnings or errors")

        val failedFiles = mutableListOf<SnapshotDataFile>()
        for ((index, snapshotFile) in snapshotFiles.withIndex()) {
            val progress = "[${index + 1}/${snapshotFiles.size}]"
            val filePath = displayPath(snapshotFile.path)
            try {
                myFixture.testHighlighting(true, false, true, snapshotFile.file)
                log("PASS $progress $filePath")
            } catch (e: AssertionError) {
                failedFiles += snapshotFile
                log("FAIL $progress $filePath")
                log(e.message.orEmpty().prependIndent("  "))
            }
        }

        val passedCount = snapshotFiles.size - failedFiles.size
        log("Result: ${passedCount} passed, ${failedFiles.size} failed, ${snapshotFiles.size} total")
        if (failedFiles.isNotEmpty()) {
            val failedPaths = failedFiles.joinToString(", ") { displayPath(it.path) }
            throw AssertionError("Highlighting snapshot test failed for ${failedFiles.size} file(s): $failedPaths")
        }
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
        return types
    }
}

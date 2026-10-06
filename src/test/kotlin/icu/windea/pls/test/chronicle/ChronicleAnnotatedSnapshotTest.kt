package icu.windea.pls.test.chronicle

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.renderers.ParadoxCsvTextAnnotatedRenderer
import icu.windea.pls.lang.util.renderers.ParadoxScriptTextAnnotatedRenderer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.data.annotated.ParadoxAnnotatedLevel
import icu.windea.pls.script.psi.ParadoxScriptFile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * 基于数种注解渲染器的快照测试。
 *
 * 执行逻辑：
 * 1. 收集并配置 [chroniclePath] 下的所有测试数据文件，同时把它们拷贝到输出目录用于差异比较。
 * 2. 对每个支持的文件（脚本/CSV）使用相应的注解渲染器，渲染出“实际”的注解文本。
 * 3. 无论测试是否通过或部分通过，都把实际渲染结果写入 `build/test-results/chronicle/.annotated` 下，
 *    与期望目录 `src/test/testData/chronicle/.annotated` 的对应文件进行差异比较。
 * 4. 输出逐文件状态与整体汇总；不支持的本地化等文件会被跳过。
 *
 * @see ChronicleSnapshotTest
 * @see ParadoxScriptTextAnnotatedRenderer
 * @see ParadoxCsvTextAnnotatedRenderer
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ChronicleAnnotatedSnapshotTest : ChronicleSnapshotTest() {
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
        logHeader("Annotated snapshot test")
        val dataFilePaths = getDataFiles()
        log("Expected annotated directory: ${chroniclePath.resolve(".annotated").toAbsolutePath()}")
        log("Actual annotated directory: ${outputPath.resolve(".annotated").toAbsolutePath()}")

        val snapshotFiles = configureDataFiles(dataFilePaths)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        copyDataFiles(dataFilePaths)

        annotateDataFiles(snapshotFiles)
    }

    private fun annotateDataFiles(snapshotFiles: List<SnapshotDataFile>) {
        val annotatedLevel = ParadoxAnnotatedLevel.ALL
        val expectedDirectory = chroniclePath.resolve(".annotated")
        val actualDirectory = outputPath.resolve(".annotated")
        val failures = mutableListOf<AnnotatedFileFailure>()
        var passedCount = 0
        var skippedCount = 0
        var generatedCount = 0

        for ((index, snapshotFile) in snapshotFiles.withIndex()) {
            val progress = "[${index + 1}/${snapshotFiles.size}]"
            val result = render(snapshotFile.file, annotatedLevel)
            if (result == null) {
                skippedCount++
                log("SKIP $progress ${displayPath(snapshotFile.path)} (unsupported file type)")
                continue
            }

            val annotatedPath = computeAnnotatedPath(snapshotFile)
            val actual = result.trimEnd()
            val actualFile = actualDirectory.resolve(annotatedPath)
            actualFile.parent?.createDirectories()
            actualFile.writeText(actual + "\n")
            generatedCount++

            val expectedFile = expectedDirectory.resolve(annotatedPath)
            val expected = if (expectedFile.exists()) {
                myFixture.configureByFile("chronicle/.annotated/$annotatedPath").text.trimEnd()
            } else null

            when {
                expected == null -> {
                    failures += AnnotatedFileFailure(annotatedPath)
                    log("FAIL $progress $annotatedPath (missing expected annotated file)")
                }
                expected != actual -> {
                    failures += AnnotatedFileFailure(annotatedPath)
                    log("FAIL $progress $annotatedPath")
                    log("  expected: ${displayPath(rootPath.relativize(expectedFile))}")
                    log("  actual:   ${displayPath(rootPath.relativize(actualFile))}")
                    log("  ${describeFirstDifference(expected, actual)}")
                }
                else -> {
                    passedCount++
                    log("PASS $progress $annotatedPath")
                }
            }
        }

        log("Generated ${generatedCount} annotated file(s) to ${actualDirectory.toAbsolutePath()}")
        log("Result: ${passedCount} passed, ${failures.size} failed, ${skippedCount} skipped, ${snapshotFiles.size} total")
        if (failures.isNotEmpty()) {
            val failedPaths = failures.joinToString(", ") { it.annotatedPath }
            throw AssertionError("Annotated snapshot test failed for ${failures.size} file(s): $failedPaths")
        }
    }

    /** 根据注入的文件信息计算注解文件路径，例如 `common/chapters/00_chapters.annotated.txt`。 */
    private fun computeAnnotatedPath(snapshotFile: SnapshotDataFile): String {
        val path = snapshotFile.file.fileInfo?.path?.path
            ?: throw IllegalStateException("Missing file info for ${displayPath(snapshotFile.path)}")
        return path.substringBeforeLast('.') + ".annotated." + path.substringAfterLast('.')
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

    /** 返回期望与实际文本的首个差异行的简要描述。 */
    private fun describeFirstDifference(expect: String, actual: String): String {
        val expectLines = expect.lines()
        val actualLines = actual.lines()
        val lineCount = maxOf(expectLines.size, actualLines.size)
        for (i in 0 until lineCount) {
            val expectLine = expectLines.getOrNull(i)
            val actualLine = actualLines.getOrNull(i)
            if (expectLine != actualLine) {
                return "first difference at line ${i + 1}: expected `$expectLine`, actual `$actualLine`"
            }
        }
        return "no line difference (only trailing/edge whitespace differs)"
    }

    private data class AnnotatedFileFailure(val annotatedPath: String)
}

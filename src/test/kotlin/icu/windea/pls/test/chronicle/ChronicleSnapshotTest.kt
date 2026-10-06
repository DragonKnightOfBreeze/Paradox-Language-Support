package icu.windea.pls.test.chronicle

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toPath
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.walk

/**
 * 快照测试的基类。
 *
 * 执行逻辑：
 * 1. 从 [chroniclePath]（即 `src/test/testData/chronicle`）下递归收集所有测试数据文件，构成快照测试的输入。
 *    隐藏目录（如 `.annotated`、`.config`）会被排除，文件名无法解析为已知文件分组（[ParadoxFileGroup]）的也会被排除。
 * 2. 具体快照测试对每个数据文件执行相应的检查（如代码检查、注解渲染），并与期望结果进行比较。
 * 3. 无论测试是否通过，执行时都会：
 *    - 把所有作为输入的测试数据文件拷贝到 [outputPath]（即 `build/test-results/chronicle`）下，保持相对目录结构；
 *    - 由子类补充生成各自的“实际输出”，便于直接与“期望输出”进行差异比较。
 *
 * 该基类集中提供输入收集、文件配置、输入拷贝以及日志输出的通用逻辑，子类只需关注具体的检查与比较方式。
 *
 * @see ChronicleInspectionBasedSnapshotTest
 * @see ChronicleAnnotatedSnapshotTest
 */
abstract class ChronicleSnapshotTest : BasePlatformTestCase(), ChronicleTestScope {
    /** 测试数据目录，相对于项目根目录。 */
    protected val rootPath = "src/test/testData".toPath()

    /** 快照测试数据目录（作为输入），相对于项目根目录。 */
    protected val chroniclePath = rootPath.resolve("chronicle")

    /** 快照测试输出目录，相对于项目根目录。用于存放输入副本与生成的实际输出，便于差异比较。 */
    protected val outputPath = "build/test-results/chronicle".toPath()

    /** 测试数据所归属的游戏类型。 */
    protected abstract val gameType: ParadoxGameType

    /**
     * 计算参与快照测试的测试数据文件路径（相对于 [rootPath]）。
     *
     * 排除隐藏目录下的文件（路径任一段以 `.` 开头），以及文件名无法解析为已知文件分组的文件。
     */
    protected fun computeDataFilePaths(): List<Path> {
        return chroniclePath.walk()
            .map { path -> rootPath.relativize(path) }
            .filter { path -> isNotHidden(path) && hasPossibleFileGroup(path) }
            .toList()
    }

    private fun isNotHidden(path: Path): Boolean = path.none { it.toString().startsWith('.') }

    private fun hasPossibleFileGroup(path: Path): Boolean = ParadoxFileGroup.resolvePossible(path.name) != ParadoxFileGroup.Other

    /**
     * 计算并校验参与快照测试的测试数据文件路径。
     *
     * 断言输入文件非空，并输出输入目录、文件数量与文件列表等日志。
     */
    protected fun getDataFiles(): List<Path> {
        val dataFilePaths = computeDataFilePaths()
        assertNotEmpty(dataFilePaths)
        log("Data directory: ${chroniclePath.toAbsolutePath()}")
        log("Output directory: ${outputPath.toAbsolutePath()}")
        log("Number of data files: ${dataFilePaths.size}")
        dataFilePaths.forEach { path -> log("- ${displayPath(path)}") }
        return dataFilePaths
    }

    /**
     * 配置（拷贝到测试项目并注入文件信息）指定的测试数据文件。
     *
     * 注入的文件路径相对于入口目录，即去除 `chronicle/` 前缀后的路径。
     */
    protected fun configureDataFiles(dataFilePaths: List<Path>): List<SnapshotDataFile> {
        return dataFilePaths.map { dataFilePath ->
            val filePath = displayPath(dataFilePath)
            val markedPath = filePath.removePrefix("chronicle/")
            markFileInfo(gameType, markedPath)
            val file = myFixture.configureByFile(filePath).virtualFile
            SnapshotDataFile(dataFilePath, file)
        }
    }

    /**
     * 把所有作为输入的测试数据文件拷贝到输出目录（[outputPath]）下，保持相对目录结构。
     *
     * 这样在执行快照测试时，可以直接在输出目录中查看输入数据，便于与期望/实际输出进行差异比较。
     */
    protected fun copyDataFiles(dataFilePaths: List<Path>) {
        for (dataFilePath in dataFilePaths) {
            val source = rootPath.resolve(dataFilePath)
            val relativePath = chroniclePath.relativize(source)
            val target = outputPath.resolve(relativePath.toString())
            target.parent?.createDirectories()
            source.copyTo(target, overwrite = true)
        }
        log("Copied ${dataFilePaths.size} input data file(s) to ${outputPath.toAbsolutePath()}")
    }

    /** 快照测试的数据文件，即输入文件路径与其在测试项目中的 [VirtualFile]。 */
    protected data class SnapshotDataFile(val path: Path, val file: VirtualFile)

    /** 返回用于日志输出的规范化路径字符串（分隔符统一为 `/`）。 */
    protected fun displayPath(path: Path): String = path.toString().normalizePath()

    /** 输出一条快照测试日志。 */
    protected fun log(message: String) = println("[chronicle] $message")

    /** 输出一个快照测试日志分段标题。 */
    protected fun logHeader(title: String) {
        println()
        println("[chronicle] ==================== $title ====================")
    }
}

package icu.windea.pls.test.chronicle

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.toPath
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.test.ChronicleTestScope
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.walk

/**
 * 快照测试的基类。
 */
abstract class ChronicleSnapshotTest : BasePlatformTestCase(), ChronicleTestScope {
    protected val rootPath = "src/test/testData".toPath()
    protected val chroniclePath = rootPath.resolve("chronicle")

    protected fun computeDataFilePaths(): List<Path> {
        return chroniclePath.walk()
            .map { path -> rootPath.relativize(path) }
            .filter { path -> isNotHidden(path) && hasPossibleFileGroup(path) }
            .toList()
    }

    private fun isNotHidden(path: Path): Boolean = path.none { it.toString().startsWith('.') }

    private fun hasPossibleFileGroup(path: Path): Boolean = ParadoxFileGroup.resolvePossible(path.name) != ParadoxFileGroup.Other
}

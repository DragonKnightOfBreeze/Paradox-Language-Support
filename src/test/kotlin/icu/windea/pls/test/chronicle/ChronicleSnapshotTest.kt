package icu.windea.pls.test.chronicle

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.toPath
import icu.windea.pls.model.ParadoxFileGroup
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.walk

abstract class ChronicleSnapshotTest : BasePlatformTestCase() {
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

package icu.windea.pls.config.configGroup

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path

/**
 * @see CwtConfigFileService
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class CwtConfigFileServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun getGameTypeFromRepoFile_allRepoDirs_test() {
        val projectRoot = findProjectRoot() ?: error("Cannot find project root")
        addAdditionalAllowedRoots(projectRoot)
        val service = CwtConfigFileService.getInstance()

        assertGameTypeFromRepoFile(service, projectRoot, "cwt/core", ParadoxGameType.Core)

        val repoDirs = Files.list(projectRoot.resolve("cwt")).use { stream ->
            stream.filter { Files.isDirectory(it) && it.fileName.toString().startsWith("cwtools-") }
                .sorted()
                .toList()
        }
        Assert.assertFalse("No config repositories found under cwt/.", repoDirs.isEmpty())
        for (repoDir in repoDirs) {
            val repoName = repoDir.fileName.toString()
            val gameTypeId = repoName.removePrefix("cwtools-").removeSuffix("-config")
            val expectedGameType = ParadoxGameType.get(gameTypeId)
            Assert.assertNotNull("Unknown game type id: $gameTypeId", expectedGameType)
            assertGameTypeFromRepoFile(service, projectRoot, "cwt/$repoName", expectedGameType!!)
        }
    }

    private fun assertGameTypeFromRepoFile(
        service: CwtConfigFileService,
        projectRoot: Path,
        relativeRepoPath: String,
        expectedGameType: ParadoxGameType,
    ) {
        val repoPath = projectRoot.resolve(relativeRepoPath)
        val cwtFile = Files.walk(repoPath).use { stream ->
            stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".cwt") }
                .sorted()
                .findFirst()
                .orElse(null)
        }
        Assert.assertNotNull("No CWT file found under $relativeRepoPath", cwtFile)

        val virtualFile = VfsUtil.findFileByIoFile(cwtFile!!.toFile(), true)
            ?: error("Cannot find virtual file: $cwtFile")
        val gameType = service.getGameTypeFromRepoFile(virtualFile)
        Assert.assertEquals(
            "repo=$relativeRepoPath, file=${virtualFile.path}",
            expectedGameType,
            gameType,
        )
    }

    private fun findProjectRoot(): Path? {
        val testDataPath = getTestDataPath().toPathOrNull()?.toAbsolutePath()?.normalize() ?: return null
        return generateSequence(testDataPath) { it.parent }
            .firstOrNull { Files.isDirectory(it.resolve(".git")) }
    }
}

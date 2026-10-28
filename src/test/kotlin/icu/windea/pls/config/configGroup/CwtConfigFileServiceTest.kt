package icu.windea.pls.config.configGroup

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.base.io.ChronicleGitService
import icu.windea.pls.config.config.CwtConfigService
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
    fun getContainingConfigGroupForRepo_coreTest() {
        val projectRoot = findProjectRoot() ?: error("Cannot find project root")
        addAdditionalAllowedRoots(projectRoot)

        val rootFile = VfsUtil.findFileByIoFile(projectRoot.toFile(), true) ?: error("Cannot find project root: $projectRoot")
        val remoteUrls = ChronicleGitService.getInstance().getRemoteUrls(rootFile)
        Assert.assertFalse(remoteUrls.toString(), remoteUrls.isEmpty())
        rootFile.putUserData(CwtConfigService.Keys.gameTypeFromRepoRoot, null)

        val file = projectRoot.resolve("cwt/core/00_aliases.cwt")
        Assert.assertTrue("Test data file does not exist: $file", Files.isRegularFile(file))
        val virtualFile = VfsUtil.findFileByIoFile(file.toFile(), true) ?: error("Cannot find virtual file: $file")
        Assert.assertTrue(
            "File is not under cwt/core: ${virtualFile.path}",
            virtualFile.path.startsWith(rootFile.path.trimEnd('/') + "/cwt/core/"),
        )

        val service = CwtConfigFileService.getInstance()

        val gameType = service.getGameTypeFromRepoFile(virtualFile)
        Assert.assertEquals(
            "remoteUrls=$remoteUrls, root=${rootFile.path}, file=${virtualFile.path}",
            ParadoxGameType.Core,
            gameType,
        )

        val configGroup = service.getContainingConfigGroupForRepo(virtualFile, project)

        Assert.assertNotNull(configGroup)
        Assert.assertEquals(ParadoxGameType.Core, configGroup?.gameType)
    }

    private fun findProjectRoot(): Path? {
        val testDataPath = getTestDataPath().toPathOrNull()?.toAbsolutePath()?.normalize() ?: return null
        return generateSequence(testDataPath) { it.parent }
            .firstOrNull { Files.isDirectory(it.resolve(".git")) }
    }
}

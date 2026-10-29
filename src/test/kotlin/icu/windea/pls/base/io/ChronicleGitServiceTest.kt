package icu.windea.pls.base.io

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.execution.ExecutionTimeouts
import icu.windea.pls.core.execution.ProcessExecutor
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * @see ChronicleGitService
 * @see ChronicleGitServiceImpl
 */
@RunWith(JUnit4::class)
class ChronicleGitServiceTest : BasePlatformTestCase() {
    private val service = ChronicleGitServiceImpl()
    private lateinit var root: Path

    @Before
    fun setUpRoot() {
        assumeGitAvailable()
        root = Path.of("build", "tmp", "base-io-git-test").toAbsolutePath().normalize()
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @Test
    fun getRepositoryPathFromUrl_test() {
        Assert.assertEquals("repo", service.getRepositoryPathFromUrl("https://example.com/repo.git"))
        Assert.assertEquals("repo", service.getRepositoryPathFromUrl("https://example.com/repo"))
    }

    @Test
    fun isUpdateToDate_test() {
        Assert.assertTrue(service.isUpdateToDate(null))
        Assert.assertTrue(service.isUpdateToDate(""))
        Assert.assertTrue(service.isUpdateToDate("Already up to date."))
        Assert.assertFalse(service.isUpdateToDate("Updated 1 file"))
    }

    @Test
    fun getRemoteUrls_test() {
        val repository = createRepository("remote-url-repo")
        runGit(repository, "remote", "add", "origin", "https://example.com/demo.git")

        val vFile = ApplicationManager.getApplication().runWriteAction<com.intellij.openapi.vfs.VirtualFile?> {
            VfsUtil.findFileByIoFile(repository.toFile(), true)
        }
        Assert.assertNotNull(vFile)
        Assert.assertEquals(listOf("https://example.com/demo.git"), service.getRemoteUrls(vFile!!))
    }

    @Test
    fun lsRemoteAndClone_test() {
        val source = createRepository("clone-source")
        val remoteUrl = remoteUrlOf(source)

        val lsOutput = service.lsRemote(remoteUrl)
        Assert.assertTrue(lsOutput.contains("refs/heads/master") || lsOutput.contains("refs/heads/main"))
        Assert.assertEquals(lsOutput, service.checkRemote(remoteUrl))

        val parent = root.resolve("clone-target").also { it.createDirectories() }
        service.clone(remoteUrl, parent)
        val cloned = parent.resolve(source.fileName.toString())
        Assert.assertTrue(Files.isRegularFile(cloned.resolve("file.txt")))
        Assert.assertEquals("initial", cloned.resolve("file.txt").readText())
    }

    @Test
    fun pull_test() {
        val source = createRepository("pull-source")
        val remoteUrl = remoteUrlOf(source)
        val parent = root.resolve("pull-target").also { it.createDirectories() }
        service.clone(remoteUrl, parent)
        val cloned = parent.resolve(source.fileName.toString())

        source.resolve("file.txt").writeText("updated")
        runGit(source, "add", ".")
        runGit(source, "commit", "-m", "update")

        val output = service.pull(remoteUrl, cloned)
        Assert.assertTrue(output.isNotEmpty())
        Assert.assertEquals("updated", cloned.resolve("file.txt").readText())
    }

    @Test
    fun syncFromRemote_test() {
        val source = createRepository("sync-source")
        val remoteUrl = remoteUrlOf(source)
        val parent = root.resolve("sync-target").also { it.createDirectories() }

        service.syncFromRemote(remoteUrl, parent.toString())
        val cloned = parent.resolve(source.fileName.toString())
        Assert.assertEquals("initial", cloned.resolve("file.txt").readText())

        source.resolve("file.txt").writeText("updated")
        runGit(source, "add", ".")
        runGit(source, "commit", "-m", "update")

        service.syncFromRemote(remoteUrl, parent.toString())
        Assert.assertEquals("updated", cloned.resolve("file.txt").readText())
    }

    private fun remoteUrlOf(path: Path): String {
        return path.toUri().toString().removeSuffix("/")
    }

    private fun createRepository(name: String): Path {
        val repository = root.resolve(name).also { it.createDirectories() }
        runGit(repository, "init")
        repository.resolve("file.txt").writeText("initial")
        runGit(repository, "add", ".")
        runGit(repository, "commit", "-m", "initial")
        return repository
    }

    private fun runGit(workDirectory: Path, vararg arguments: String) {
        val commandLine = GeneralCommandLine(
            listOf(
                "git",
                "-c", "user.name=Chronicle Test",
                "-c", "user.email=chronicle-test@example.com",
            ) + arguments
        ).withWorkingDirectory(workDirectory).withCharset(Charsets.UTF_8)
        val output = ProcessExecutor(commandLine).withTimeout(ExecutionTimeouts.normal).execute()
        Assert.assertEquals("git ${arguments.joinToString(" ")} failed: ${output.stderr}", 0, output.exitCode)
    }

    private fun assumeGitAvailable() {
        val output = runCatching {
            ProcessExecutor(GeneralCommandLine("git", "--version"))
                .withTimeout(ExecutionTimeouts.short)
                .execute()
        }.getOrNull()
        Assume.assumeTrue("Git is not available", output != null && output.exitCode == 0)
    }
}

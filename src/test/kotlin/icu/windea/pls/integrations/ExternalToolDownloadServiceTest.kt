package icu.windea.pls.integrations

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * @see ExternalToolDownloadService
 */
@RunWith(JUnit4::class)
class ExternalToolDownloadServiceTest {
    private val service = ExternalToolDownloadService()

    @Test
    fun check_nullOrMissing_test() {
        val dir = testDirectory().resolve("missing")
        Files.deleteIfExists(dir)
        Assert.assertTrue(service.check(null))
        Assert.assertTrue(service.check(dir))
    }

    @Test
    fun check_empty_test() {
        val dir = testDirectory().resolve("empty").also {
            it.toFile().deleteRecursively()
            it.createDirectories()
        }
        Assert.assertTrue(service.check(dir))

        val file = dir.resolve("empty.txt").also { Files.writeString(it, "") }
        Assert.assertTrue(service.check(file))

        Files.writeString(file, "not empty")
        Assert.assertFalse(service.check(file))
        Assert.assertFalse(service.check(dir))
    }

    private fun testDirectory(): Path {
        return Path.of("build", "tmp", "external-tool-download-service-test").also { it.createDirectories() }
    }
}

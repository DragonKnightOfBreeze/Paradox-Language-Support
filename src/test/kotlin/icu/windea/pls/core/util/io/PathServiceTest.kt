package icu.windea.pls.core.util.io

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @see PathService
 */
class PathServiceTest {
    private val root: Path = Path.of("build/tmp/path-service-test")

    @Before
    fun setUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @After
    fun tearDown() {
        root.toFile().deleteRecursively()
    }

    @Test
    fun testEnsureDirectoryCreated_NotExists() {
        val dir = root.resolve("new_dir")
        PathService.ensureDirectoryCreated(dir)
        assertTrue(dir.isDirectory())
    }

    @Test
    fun testEnsureDirectoryCreated_ExistsAsFile() {
        val dir = root.resolve("existing_file")
        dir.createFile()
        assertTrue(dir.isRegularFile())

        PathService.ensureDirectoryCreated(dir)
        assertTrue(dir.isDirectory())
    }

    @Test
    fun testEnsureFileCreatedFromClasspath_NotExists() {
        val file = root.resolve("new_file.txt")
        PathService.ensureFileCreatedFromClasspath(file, "/messages/ChronicleBundle.properties")
        assertTrue(file.isRegularFile())
        assertTrue(file.fileSize() > 0)
    }

    @Test
    fun testEnsureFileCreatedFromClasspath_ExistsAsDirectory() {
        val file = root.resolve("existing_dir")
        file.createDirectories()
        assertTrue(file.isDirectory())

        PathService.ensureFileCreatedFromClasspath(file, "/messages/ChronicleBundle.properties")
        assertTrue(file.isRegularFile())
        assertTrue(file.fileSize() > 0)
    }

    @Test
    fun testEnsureFileCreatedFromClasspath_ExistsAsEmptyFile() {
        val file = root.resolve("empty_file.txt")
        file.createFile()
        assertTrue(file.isRegularFile())
        assertEquals(0L, file.fileSize())

        PathService.ensureFileCreatedFromClasspath(file, "/messages/ChronicleBundle.properties")
        assertTrue(file.isRegularFile())
        assertTrue(file.fileSize() > 0)
    }

    @Test
    fun testEnsureFileCreatedFromClasspath_ExistsAsNonEmptyFile() {
        val file = root.resolve("non_empty_file.txt")
        file.writeText("dummy")
        assertTrue(file.isRegularFile())
        assertEquals(5L, file.fileSize())

        PathService.ensureFileCreatedFromClasspath(file, "/messages/ChronicleBundle.properties")
        assertTrue(file.isRegularFile())
        assertEquals(5L, file.fileSize()) // Should not be overwritten
    }

    @Test
    fun findExecutable_test() {
        val dir = root.resolve("find-executable")
        val executable = dir.resolve("bin/tool").also { it.parent.createDirectories() }
        Files.writeString(executable, "")
        Assert.assertEquals(executable, PathService.findExecutable(dir, "tool"))
        Assert.assertNull(PathService.findExecutable(dir, "missing"))
    }
}

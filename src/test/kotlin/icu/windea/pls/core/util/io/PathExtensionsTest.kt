package icu.windea.pls.core.util.io

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * @see toPath
 * @see toPathOrNull
 * @see parsePath
 * @see clean
 * @see moveToAtomically
 * @see deleteQuietly
 */
@RunWith(JUnit4::class)
class PathExtensionsTest {
    private val root = Path.of("build", "tmp", "path-extensions-test")

    @Before
    fun doSetUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @Test
    fun toPath_test() {
        Assert.assertEquals(Path.of("path/to/file.txt"), "path/to/file.txt".toPath())
    }

    @Test
    fun toPathOrNull_test() {
        Assert.assertEquals(Path.of("path/to/file.txt"), "path/to/file.txt".toPathOrNull())
        Assert.assertNull("\u0000".toPathOrNull())
    }

    @Test
    fun parsePath_test() {
        Assert.assertEquals(Path.of("path/to/file.txt"), "  path/to/file.txt  ".parsePath())
        Assert.assertNull("   ".parsePath())
        Assert.assertNull((null as String?).parsePath())
    }

    @Test
    fun clean_test() {
        val path = Path.of("build", "tmp", "..", "tmp", "file.txt")
        val cleaned = path.clean()
        Assert.assertTrue(cleaned.isAbsolute)
        Assert.assertFalse(cleaned.toString().contains(".."))
    }

    @Test
    fun moveToAtomically_movesFile_test() {
        val source = root.resolve("move-source.txt").also { Files.writeString(it, "source") }
        val target = root.resolve("move-target.txt")

        val result = source.moveToAtomically(target)

        Assert.assertEquals(target, result)
        Assert.assertFalse(source.exists())
        Assert.assertEquals("source", Files.readString(target))
    }

    @Test
    fun moveToAtomically_overwritesExistingTarget_test() {
        val source = root.resolve("overwrite-source.txt").also { Files.writeString(it, "new") }
        val target = root.resolve("overwrite-target.txt").also { Files.writeString(it, "old") }

        source.moveToAtomically(target, overwrite = true)

        Assert.assertFalse(source.exists())
        Assert.assertEquals("new", Files.readString(target))
    }

    @Test
    fun moveToAtomically_withoutOverwriteToNewTarget_test() {
        val source = root.resolve("no-overwrite-source.txt").also { Files.writeString(it, "source") }
        val target = root.resolve("no-overwrite-target.txt")

        source.moveToAtomically(target, overwrite = false)

        Assert.assertFalse(source.exists())
        Assert.assertEquals("source", Files.readString(target))
    }

    @Test
    fun deleteQuietly_existingFile_test() {
        val file = root.resolve("deletable.txt").also { Files.writeString(it, "content") }

        Assert.assertTrue(file.deleteQuietly())
        Assert.assertFalse(file.exists())
    }

    @Test
    fun deleteQuietly_missingFile_test() {
        val file = root.resolve("missing.txt")

        Assert.assertFalse(file.deleteQuietly())
        Assert.assertFalse(file.exists())
    }

    @Test
    fun deleteQuietly_recursiveDirectory_test() {
        val directory = root.resolve("recursive").also { it.createDirectories() }
        val nested = directory.resolve("nested").also { it.createDirectories() }
        Files.writeString(nested.resolve("file.txt"), "content")

        Assert.assertTrue(directory.deleteQuietly(recursive = true))
        Assert.assertFalse(directory.exists())
    }

    @Test
    fun deleteQuietly_missingDirectoryWithRecursive_test() {
        val directory = root.resolve("missing-recursive")

        // 路径不存在时视为已经清理完成
        Assert.assertTrue(directory.deleteQuietly(recursive = true))
    }

    @Test
    fun deleteQuietly_nonEmptyDirectoryWithoutRecursive_test() {
        val directory = root.resolve("non-recursive").also { it.createDirectories() }
        Files.writeString(directory.resolve("file.txt"), "content")

        // 非递归删除无法删除非空目录
        Assert.assertFalse(directory.deleteQuietly())
        Assert.assertTrue(directory.exists())

        Files.delete(directory.resolve("file.txt"))
        Assert.assertTrue(directory.deleteQuietly())
        Assert.assertFalse(directory.exists())
    }
}

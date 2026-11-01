package icu.windea.pls.core.util.io

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path

/**
 * @see toPath
 * @see toPathOrNull
 * @see parsePath
 * @see clean
 */
@RunWith(JUnit4::class)
class PathExtensionsTest {
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
}

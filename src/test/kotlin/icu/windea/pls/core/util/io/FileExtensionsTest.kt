package icu.windea.pls.core.util.io

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

/**
 * @see toFile
 * @see toFileOrNull
 */
@RunWith(JUnit4::class)
class FileExtensionsTest {
    @Test
    fun toFile_test() {
        Assert.assertEquals(File("path/to/file.txt"), "path/to/file.txt".toFile())
    }

    @Test
    fun toFileOrNull_test() {
        Assert.assertEquals(File("path/to/file.txt"), "path/to/file.txt".toFileOrNull())
    }
}

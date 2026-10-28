package icu.windea.pls.images

import icu.windea.pls.images.policies.ImageCacheCleanupPolicy
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories

/**
 * @see ImageCacheCleaner
 */
@RunWith(JUnit4::class)
class ImageCacheCleanerTest {
    @Test
    fun cleanup_byTtl_test() {
        val dir = Path.of("build", "tmp", "image-cache-cleaner-test").also { it.createDirectories() }
        val oldFile = dir.resolve("old.png")
        val newFile = dir.resolve("new.png")
        Files.write(oldFile, byteArrayOf(1))
        Files.write(newFile, byteArrayOf(1))
        val oldTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40))
        Files.setLastModifiedTime(oldFile, oldTime)

        val deleted = ImageCacheCleaner(dir).cleanup(ImageCacheCleanupPolicy.ByTtl)

        Assert.assertEquals(1, deleted)
        Assert.assertFalse(Files.exists(oldFile))
        Assert.assertTrue(Files.exists(newFile))
    }

    @Test
    fun cleanup_none_test() {
        val dir = Path.of("build", "tmp", "image-cache-cleaner-none-test").also { it.createDirectories() }
        val file = dir.resolve("old.png")
        Files.write(file, byteArrayOf(1))
        val oldTime = FileTime.fromMillis(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40))
        Files.setLastModifiedTime(file, oldTime)

        val deleted = ImageCacheCleaner(dir).cleanup(ImageCacheCleanupPolicy.None)

        Assert.assertEquals(0, deleted)
        Assert.assertTrue(Files.exists(file))
    }
}

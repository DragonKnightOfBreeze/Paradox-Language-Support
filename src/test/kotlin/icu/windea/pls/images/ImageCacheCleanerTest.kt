package icu.windea.pls.images

import icu.windea.pls.model.policies.ImageCacheCleanupPolicy
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
    fun cleanup_bySize_test() {
        val dir = Path.of("build", "tmp", "image-cache-cleaner-size-test").also { it.createDirectories() }
        val files = (1..4).map { i -> dir.resolve("file$i.png").also { Files.write(it, ByteArray(2)) } }
        val now = System.currentTimeMillis()
        files.forEachIndexed { index, file ->
            Files.setLastModifiedTime(file, FileTime.fromMillis(now - TimeUnit.DAYS.toMillis((4 - index).toLong())))
        }

        // 总大小为 8 字节，限制为 5 字节：删除最旧的两个文件
        val deleted = ImageCacheCleaner(dir, maxSize = 5, maxFiles = 10).cleanup(ImageCacheCleanupPolicy.BySize)

        Assert.assertEquals(2, deleted)
        Assert.assertFalse(Files.exists(files[0]))
        Assert.assertFalse(Files.exists(files[1]))
        Assert.assertTrue(Files.exists(files[2]))
        Assert.assertTrue(Files.exists(files[3]))
    }

    @Test
    fun cleanup_byTtlAndSize_test() {
        val dir = Path.of("build", "tmp", "image-cache-cleaner-ttl-size-test").also { it.createDirectories() }
        val oldFile = dir.resolve("old.png").also { Files.write(it, ByteArray(2)) }
        val newFiles = (1..3).map { i -> dir.resolve("new$i.png").also { Files.write(it, ByteArray(2)) } }
        val now = System.currentTimeMillis()
        Files.setLastModifiedTime(oldFile, FileTime.fromMillis(now - TimeUnit.DAYS.toMillis(40)))
        newFiles.forEachIndexed { index, file ->
            Files.setLastModifiedTime(file, FileTime.fromMillis(now - TimeUnit.DAYS.toMillis((3 - index).toLong())))
        }

        // TTL 删除过期文件；剩余总大小 6 字节超过 4 字节，再删除最旧的一个
        val deleted = ImageCacheCleaner(dir, maxSize = 4, maxFiles = 10)
            .cleanup(ImageCacheCleanupPolicy.ByTtlAndSize)

        Assert.assertEquals(2, deleted)
        Assert.assertFalse(Files.exists(oldFile))
        Assert.assertFalse(Files.exists(newFiles[0]))
        Assert.assertTrue(Files.exists(newFiles[1]))
        Assert.assertTrue(Files.exists(newFiles[2]))
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

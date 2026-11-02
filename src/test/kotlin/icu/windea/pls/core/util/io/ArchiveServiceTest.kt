package icu.windea.pls.core.util.io

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.io.path.readText

/**
 * @see ArchiveService
 */
@RunWith(JUnit4::class)
class ArchiveServiceTest {
    private val root = Path.of("build/tmp/archieve-service-test")

    @Before
    fun setUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @Test
    fun extractZip_test() {
        val dir = root.resolve("zip")
        dir.createDirectories()
        val archive = dir.resolve("test.zip")
        ZipOutputStream(archive.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("dir/file.txt"))
            zip.write("hello".toByteArray())
            zip.closeEntry()
        }
        val target = dir.resolve("out").also { it.createDirectories() }
        ArchiveService.extract(archive, target, ArchiveType.Zip)
        Assert.assertEquals("hello", target.resolve("dir/file.txt").readText())
    }

    @Test
    fun extractTarGz_test() {
        val dir = root.resolve("tar-gz")
        dir.createDirectories()
        val archive = dir.resolve("test.tar.gz")
        GzipCompressorOutputStream(archive.outputStream().buffered()).use { gzip ->
            TarArchiveOutputStream(gzip).use { tar ->
                val bytes = "hello".toByteArray()
                val entry = TarArchiveEntry("file.txt").apply { size = bytes.size.toLong() }
                tar.putArchiveEntry(entry)
                tar.write(bytes)
                tar.closeArchiveEntry()
            }
        }
        val target = dir.resolve("out").also { it.createDirectories() }
        ArchiveService.extract(archive, target, ArchiveType.TarGz)
        Assert.assertEquals("hello", target.resolve("file.txt").readText())
    }

    @Test
    fun extractSevenZip_test() {
        val dir = root.resolve("seven-zip")
        dir.createDirectories()
        val archive = dir.resolve("test.7z")
        SevenZOutputFile(archive.toFile()).use { sevenZ ->
            val bytes = "hello".toByteArray()
            val entry = SevenZArchiveEntry().apply {
                name = "file.txt"
                size = bytes.size.toLong()
            }
            sevenZ.putArchiveEntry(entry)
            sevenZ.write(bytes)
            sevenZ.closeArchiveEntry()
        }
        val target = dir.resolve("out").also { it.createDirectories() }
        ArchiveService.extract(archive, target, ArchiveType.SevenZip, ArchiveOptions(allowExternalSevenZip = false))
        Assert.assertEquals("hello", target.resolve("file.txt").readText())
    }
}

package icu.windea.pls.core.util.io

import icu.windea.pls.core.util.ImageFrameInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.readBytes

/**
 * @see ImageIoService
 */
@RunWith(JUnit4::class)
class ImageIoServiceTest {
    private val root = Path.of("build/tmp/image-io-service-test")

    @Before
    fun setUp() {
        root.toFile().deleteRecursively()
        root.createDirectories()
    }

    @Test
    fun isSameImageFormat_test() {
        Assert.assertTrue(ImageIoService.isSameImageFormat("png", ".PNG"))
        Assert.assertTrue(ImageIoService.isSameImageFormat(" .jpg ", "jpg"))
        Assert.assertFalse(ImageIoService.isSameImageFormat("png", "jpg"))
        Assert.assertEquals("PNG", ImageIoService.normalizeImageFormat(" .PNG "))
    }

    @Test
    fun readWriteImage_test() {
        val path = root.resolve("roundtrip.png")
        val sourceImage = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)

        Assert.assertTrue(ImageIoService.writeImage(sourceImage, "png", path))
        Assert.assertTrue(Files.isRegularFile(path))

        val targetImage = ImageIoService.readImage(path)
        Assert.assertEquals(sourceImage.width, targetImage.width)
        Assert.assertEquals(sourceImage.height, targetImage.height)
        Assert.assertEquals(10, ImageIoService.readImage(path).width)
    }

    @Test
    fun readWriteImage_stream_test() {
        val sourceImage = BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB)
        val output = ByteArrayOutputStream()
        Assert.assertTrue(ImageIoService.writeImage(sourceImage, "png", output))

        val targetImage = ImageIoService.readImage(ByteArrayInputStream(output.toByteArray()))
        Assert.assertEquals(4, targetImage.width)
        Assert.assertEquals(4, targetImage.height)
    }

    @Test
    fun scaleImage_test() {
        val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
        val sameImage = ImageIoService.scaleImage(image, null, null)
        Assert.assertSame(image, sameImage)
        Assert.assertSame(image, ImageIoService.scaleImage(image, 20, 20))

        val scaled = ImageIoService.scaleImage(image, 5, 5)
        Assert.assertEquals(5, scaled.width)
        Assert.assertEquals(5, scaled.height)
    }

    @Test
    fun sliceImage_test() {
        val image = BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB)
        val sliced = ImageIoService.sliceImage(image, ImageFrameInfo.of(1, 2))
        Assert.assertNotNull(sliced)
        Assert.assertEquals(10, sliced!!.width)
        Assert.assertEquals(10, sliced.height)

        val second = ImageIoService.sliceImage(image, ImageFrameInfo.of(2, 2))
        Assert.assertNotNull(second)
        Assert.assertEquals(10, second!!.width)

        Assert.assertNull(ImageIoService.sliceImage(image, ImageFrameInfo.of(3, 2)))
        Assert.assertNull(ImageIoService.sliceImage(image, ImageFrameInfo.of(0, 0)))
    }

    @Test
    fun copyIfSameFormat_test() {
        val source = root.resolve("same-source.png")
        val target = root.resolve("same-target.png")
        val image = BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB)
        ImageIO.write(image, "png", source.toFile())

        Assert.assertFalse(ImageIoService.copyIfSameFormat(source, target, "png", "jpg"))
        Assert.assertFalse(Files.exists(target))

        Assert.assertTrue(ImageIoService.copyIfSameFormat(source, target, "png", ".PNG"))
        Assert.assertTrue(Files.readAllBytes(target).contentEquals(source.readBytes()))
    }

    @Test
    fun convertImageFormat_test() {
        val source = root.resolve("convert-source.png")
        val target = root.resolve("convert-target.jpg")
        ImageIO.write(BufferedImage(6, 4, BufferedImage.TYPE_INT_ARGB), "png", source.toFile())

        ImageIoService.convertImageFormat(source, target, "jpg")

        Assert.assertTrue(Files.isRegularFile(target))
        val targetImage = ImageIoService.readImage(target)
        Assert.assertEquals(6, targetImage.width)
        Assert.assertEquals(4, targetImage.height)
    }

}

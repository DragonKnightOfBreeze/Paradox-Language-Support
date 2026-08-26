package icu.windea.pls.core.io

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import javax.imageio.stream.MemoryCacheImageInputStream

/**
 * @see ImageInputStreamAdapter
 */
class ImageInputStreamAdapterTest {
    private fun adapter(bytes: ByteArray) = ImageInputStreamAdapter(MemoryCacheImageInputStream(ByteArrayInputStream(bytes)))

    @Test
    fun read_single_test() {
        val a = adapter("abc".toByteArray())
        Assert.assertEquals('a'.code, a.read())
        Assert.assertEquals('b'.code, a.read())
        Assert.assertEquals('c'.code, a.read())
        Assert.assertEquals(-1, a.read())
    }

    @Test
    fun read_byteArray_test() {
        val a = adapter("abc".toByteArray())
        val buf = ByteArray(2)
        Assert.assertEquals(2, a.read(buf))
        Assert.assertArrayEquals("ab".toByteArray(), buf)
        val buf2 = ByteArray(2)
        Assert.assertEquals(1, a.read(buf2))
        Assert.assertEquals('c'.code.toByte(), buf2[0])
        Assert.assertEquals(-1, a.read(buf2))
    }

    @Test
    fun read_byteArray_offset_len_test() {
        val a = adapter("abcde".toByteArray())
        val buf = ByteArray(5)
        Assert.assertEquals(3, a.read(buf, 1, 3))
        Assert.assertArrayEquals(byteArrayOf(0, 'a'.code.toByte(), 'b'.code.toByte(), 'c'.code.toByte(), 0), buf)
    }

    @Test
    fun skip_test() {
        val a = adapter("abcdef".toByteArray())
        Assert.assertEquals(2L, a.skip(2))
        Assert.assertEquals('c'.code, a.read())
    }

    @Test
    fun mark_reset_test() {
        val a = adapter("abcdef".toByteArray())
        Assert.assertTrue(a.markSupported())
        a.read() // 'a'
        a.read() // 'b'
        a.mark(0)
        a.read() // 'c'
        a.read() // 'd'
        a.reset()
        Assert.assertEquals('c'.code, a.read()) // 回到 mark 位置
    }

    @Test
    fun available_test() {
        Assert.assertEquals(0, adapter("abc".toByteArray()).available())
    }

    @Test
    fun close_test() {
        val a = adapter("abc".toByteArray())
        a.close() // 委托关闭，不抛异常即视为成功
    }
}

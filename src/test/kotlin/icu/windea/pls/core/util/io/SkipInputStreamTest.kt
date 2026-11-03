package icu.windea.pls.core.util.io

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayInputStream

/**
 * @see SkipInputStream
 * @see skip
 * @see skipUtf8Bom
 */
@RunWith(JUnit4::class)
class SkipInputStreamTest {
    // region skip

    @Test
    fun skip_matchedPrefix() {
        val input = ByteArrayInputStream("hello".toByteArray())
        val result = input.skip("he".toByteArray())

        Assert.assertEquals("llo", result.readAllBytes().decodeToString())
    }

    @Test
    fun skip_unmatchedPrefix_replaysBytes() {
        val input = ByteArrayInputStream("hello".toByteArray())
        val result = input.skip("HE".toByteArray())

        Assert.assertEquals("hello", result.readAllBytes().decodeToString())
    }

    @Test
    fun skip_emptyPrefix() {
        val input = ByteArrayInputStream("hello".toByteArray())
        val result = input.skip(ByteArray(0))

        Assert.assertSame(input, result)
        Assert.assertEquals("hello", result.readAllBytes().decodeToString())
    }

    @Test
    fun skip_shortInputStream_replaysBytes() {
        val input = ByteArrayInputStream("he".toByteArray())
        val result = input.skip("hello".toByteArray())

        Assert.assertEquals("he", result.readAllBytes().decodeToString())
    }

    // endregion

    // region skipUtf8Bom

    @Test
    fun skipUtf8Bom_withBom() {
        val input = ByteArrayInputStream(UTF8_BOM + "hello".toByteArray())
        val result = input.skipUtf8Bom()

        Assert.assertEquals("hello", result.readAllBytes().decodeToString())
    }

    @Test
    fun skipUtf8Bom_withoutBom() {
        val input = ByteArrayInputStream("hello".toByteArray())
        val result = input.skipUtf8Bom()

        Assert.assertEquals("hello", result.readAllBytes().decodeToString())
    }

    // endregion

    // region read / skip

    @Test
    fun readBatch_andSkip() {
        val input = ByteArrayInputStream(UTF8_BOM + "hello".toByteArray())
        val result = SkipInputStream(input, UTF8_BOM)

        val buffer = ByteArray(2)
        Assert.assertEquals(2, result.read(buffer))
        Assert.assertEquals("he", buffer.decodeToString())
        Assert.assertEquals(2L, result.skip(2))
        Assert.assertEquals("o", result.read().toChar().toString())
        Assert.assertEquals(-1, result.read())
    }

    // endregion
}

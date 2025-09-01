package icu.windea.pls.core.io

import java.io.IOException
import java.io.InputStream
import javax.imageio.stream.ImageInputStream

/**
 * 将 [ImageInputStream] 适配为 `InputStream`。
 *
 * - 支持 `mark/reset`（基于 `streamPosition`）。
 * - 其它读写行为直接委托给底层的 [imageInputStream]。
 */
class ImageInputStreamAdapter(
    val imageInputStream: ImageInputStream
) : InputStream() {
    private var lastMarkPosition: Long = 0

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return imageInputStream.read(b, off, len)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return imageInputStream.read(b)
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return imageInputStream.read()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        return imageInputStream.skipBytes(n)
    }

    @Throws(IOException::class)
    override fun close() {
        imageInputStream.close()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun mark(readlimit: Int) {
        lastMarkPosition = imageInputStream.streamPosition
    }

    override fun markSupported(): Boolean {
        return true
    }

    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        imageInputStream.seek(lastMarkPosition)
    }

    @Throws(IOException::class)
    override fun available(): Int {
        return 0
    }
}

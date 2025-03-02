package icu.windea.pls.core.io

import java.io.*
import javax.imageio.stream.*

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

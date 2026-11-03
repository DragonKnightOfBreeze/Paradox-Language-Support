package icu.windea.pls.core.util.io

import java.io.InputStream

/**
 * 用于跳过指定字节序列的 [InputStream]。
 *
 * 如果输入流以指定的字节序列 [prefix] 开头，则读取时会跳过它；否则会按原样重新提供已读取的部分。
 * 因此，这个类可以安全地用于检测并跳过可选的字节序列（例如 UTF-8 BOM），而不会丢弃实际内容。
 *
 * 实现上，初始化时会预读至多 `prefix.size` 个字节，并在需要时回放。
 * 这类似于 `PushbackInputStream`，但可以直接指定要跳过（匹配时）或回放（不匹配时）的字节序列。
 *
 * @see InputStream.skip
 * @see InputStream.skipUtf8Bom
 */
class SkipInputStream(
    private val input: InputStream,
    private val prefix: ByteArray,
) : InputStream() {
    private val replay: ByteArray
    private var replayIndex = 0

    init {
        if (prefix.isEmpty()) {
            replay = emptyByteArray
        } else {
            val buffer = ByteArray(prefix.size)
            var count = 0
            while (count < buffer.size) {
                val n = input.read(buffer, count, buffer.size - count)
                if (n == -1) break
                count += n
            }
            val matched = count == prefix.size && buffer.contentEquals(prefix)
            replay = if (matched) emptyByteArray else buffer.copyOf(count)
        }
    }

    override fun read(): Int {
        if (replayIndex < replay.size) return replay[replayIndex++].toInt() and 0xFF
        return input.read()
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        if (replayIndex < replay.size) {
            val n = minOf(len, replay.size - replayIndex)
            System.arraycopy(replay, replayIndex, b, off, n)
            replayIndex += n
            return n
        }
        return input.read(b, off, len)
    }

    override fun skip(n: Long): Long {
        if (n <= 0) return 0
        var remaining = n
        if (replayIndex < replay.size) {
            val skipped = minOf(remaining, (replay.size - replayIndex).toLong())
            replayIndex += skipped.toInt()
            remaining -= skipped
        }
        if (remaining <= 0) return n - remaining
        return (n - remaining) + input.skip(remaining)
    }

    override fun available(): Int {
        return (replay.size - replayIndex) + input.available()
    }

    override fun close() {
        input.close()
    }

    private companion object {
        val emptyByteArray = ByteArray(0)
    }
}

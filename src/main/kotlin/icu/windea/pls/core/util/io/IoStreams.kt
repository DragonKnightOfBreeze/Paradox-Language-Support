@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util.io

import java.io.InputStream
import java.io.OutputStream

/** IO 中进行取消检查的默认字节阈值。 */
const val IO_CANCELLATION_THRESHOLD = 64L * 1024

/** UTF-8 BOM（`EF BB BF`）。 */
val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

/** 将输入流包装为可取消的输入流（[CancellableInputStream]）。 */
inline fun InputStream.asCancellable(threshold: Long = IO_CANCELLATION_THRESHOLD): CancellableInputStream {
    return CancellableInputStream(this, threshold)
}

/** 将输出流包装为可取消的输出流（[CancellableOutputStream]）。 */
inline fun OutputStream.asCancellable(threshold: Long = IO_CANCELLATION_THRESHOLD): CancellableOutputStream {
    return CancellableOutputStream(this, threshold)
}

/**
 * 将输入流包装为跳过指定字节序列（[prefix]，如果存在）的输入流（[SkipInputStream]）。
 *
 * 如果输入流不以 [prefix] 开头，则已读取的字节会被原样重新提供，不会丢失内容。
 */
fun InputStream.skip(prefix: ByteArray): InputStream {
    if (prefix.isEmpty()) return this
    return SkipInputStream(this, prefix)
}

/** 将输入流包装为跳过 UTF-8 BOM（[UTF8_BOM]，如果存在）的输入流（[SkipInputStream]）。 */
fun InputStream.skipUtf8Bom(): InputStream {
    return skip(UTF8_BOM)
}

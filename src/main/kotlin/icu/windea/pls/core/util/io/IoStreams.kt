@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util.io

import java.io.InputStream
import java.io.OutputStream

/** IO 中进行取消检查的默认字节阈值。 */
const val IO_CANCELLATION_THRESHOLD = 64L * 1024

/** 将输入流包装为可取消的输入流（[CancellableInputStream]）。 */
inline fun InputStream.asCancellable(threshold: Long = IO_CANCELLATION_THRESHOLD): CancellableInputStream {
    return CancellableInputStream(this, threshold)
}

/** 将输出流包装为可取消的输出流（[CancellableOutputStream]）。 */
inline fun OutputStream.asCancellable(threshold: Long = IO_CANCELLATION_THRESHOLD): CancellableOutputStream {
    return CancellableOutputStream(this, threshold)
}

package icu.windea.pls.core.util.io

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object IoService {
    /**
     * 复制文件（并进行额外的预检查和处理），返回已复制的字节长度。
     *
     * 备注：
     * - 如果 [options] 启用了取消检查，会在读取的字节数达到阈值时进行取消检查。
     * - 如果 [sourcePath] 与 [targetPath] 相同，则直接跳过操作。
     * - 如果 [targetPath] 的父目录未创建，则尝试创建。
     */
    fun copy(
        sourcePath: Path,
        targetPath: Path,
        options: IoOptions = IoOptions.DEFAULT,
    ): Long {
        // `.bufferred()` is not necessary and suggested in this implementation

        if (sourcePath.clean() == targetPath.clean()) return -1
        targetPath.parent?.createDirectories()
        return sourcePath.inputStream().use { inputStream ->
            targetPath.outputStream().use { outputStream ->
                copy(inputStream, outputStream, options)
            }
        }
    }

    /**
     * 复制流（并进行额外的预检查和处理），返回已复制的字节长度。
     *
     * 备注：
     * - 如果 [options] 启用了取消检查，会在读取的字节数达到阈值时进行取消检查。
     * - [inputStream] 和 [outputStream] 不会被自动关闭。
     */
    fun copy(
        inputStream: InputStream,
        outputStream: OutputStream,
        options: IoOptions = IoOptions.DEFAULT,
    ): Long {
        // `.bufferred()` is not necessary and suggested in this implementation
        // `inputStream` and `outputStream` will not be auto-closed in this implementation, so it's better to flush

        val source = if (options.cancellable) inputStream.asCancellable(options.cancellationThreshold) else inputStream
        val bytesCopied = source.copyTo(outputStream, options.bufferSize.coerceAtLeast(1))
        outputStream.flush() // it's better to flush explicitly here
        return bytesCopied
    }
}

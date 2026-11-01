package icu.windea.pls.integrations.images.providers

import com.intellij.execution.ExecutionException
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.core.util.io.ImageInputStreamAdapter
import icu.windea.pls.core.util.io.ImageIoService
import icu.windea.pls.core.util.io.IoService
import icu.windea.pls.integrations.images.ImageToolProvider
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.outputStream

abstract class CommandBasedImageToolProvider : ImageToolProvider {
    final override fun isAvailable(): Boolean {
        return isEnabled() && isSupported() && isValid()
    }

    abstract fun isEnabled(): Boolean

    abstract fun isSupported(): Boolean

    open fun isValid(): Boolean {
        val executablePath = getExecutablePath() ?: return false
        return validateExecutablePath(executablePath)
    }

    abstract fun getExecutablePath(): Path?

    abstract fun validateExecutablePath(path: Path): Boolean

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    final override fun read(imageIndex: Int, param: ImageReadParam?, stream: ImageInputStream, sourceFormat: String, targetFormat: String): BufferedImage {
        val inputStream = ImageInputStreamAdapter(stream)
        val pathsToDelete = mutableSetOf<Path>()
        try {
            val tempParentPath = ChronicleDataPathService.getInstance().imagesTempPath
            val path = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            pathsToDelete.add(path)
            path.outputStream().use { IoService.copy(inputStream, it) }
            val targetPath = runBlocking { convertImageFormat(path, null, null, sourceFormat, targetFormat) }
            pathsToDelete.add(targetPath)
            return targetPath.inputStream().use { ImageIoService.readImage(it) }
        } finally {
            pathsToDelete.forEach { it.deleteIfExists() } // 确保删除临时文件
        }
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    final override suspend fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(inputStream, outputStream, sourceFormat, targetFormat)) return
        // implementation
        val pathsToDelete = mutableSetOf<Path>()
        try {
            val tempParentPath = ChronicleDataPathService.getInstance().imagesTempPath
            val path = tempParentPath.resolve(UUID.randomUUID().toString() + "." + sourceFormat)
            pathsToDelete.add(path)
            path.outputStream().use { IoService.copy(inputStream, it) }
            val targetPath = convertImageFormat(path, null, null, sourceFormat, targetFormat)
            pathsToDelete.add(targetPath)
            targetPath.inputStream().use { IoService.copy(it, outputStream) }
        } finally {
            pathsToDelete.forEach { it.deleteIfExists() } // 确保删除临时文件
        }
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    final override suspend fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String) {
        // 3.0.5 fast return if necessary
        if (ImageIoService.copyIfSameFormat(path, targetPath, sourceFormat, targetFormat)) return
        // implementation
        convertImageFormat(path, targetPath.parent, targetPath.name, sourceFormat, targetFormat)
    }

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    abstract suspend fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path
}

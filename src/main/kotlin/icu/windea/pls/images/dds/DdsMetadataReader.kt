@file:Suppress("unused")

package icu.windea.pls.images.dds

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import icu.windea.pls.core.runCatchingCancellable
import icu.windea.pls.images.dds.info.DdsInfoReader
import java.io.InputStream

/**
 * @see DdsInfoReader
 */
object DdsMetadataReader {
    @RequiresReadLock(generateAssertion = false)
    fun readOrNull(file: VirtualFile): DdsMetadata? {
        return runCatchingCancellable { readInternal(file) }
            .getOrNull()
    }

    fun readOrNull(inputStream: InputStream): DdsMetadata? {
        return runCatchingCancellable { readInternal(inputStream) }
            .getOrNull()
    }

    @RequiresReadLock(generateAssertion = false)
    fun read(file: VirtualFile): DdsMetadata {
        return runCatchingCancellable { readInternal(file) }
            .getOrElse { e -> throw IllegalStateException("Cannot read dds metadata from file ${file.path}.", e) }
    }

    fun read(inputStream: InputStream): DdsMetadata {
        return runCatchingCancellable { readInternal(inputStream) }
            .getOrElse { e -> throw IllegalStateException("Cannot read dds metadata from input stream.", e) }
    }

    private fun readInternal(file: VirtualFile): DdsMetadata {
        return readInternal(file.inputStream)
    }

    private fun readInternal(inputStream: InputStream): DdsMetadata {
        // org.intellij.images.util.ImageInfoReader.read
        val info = inputStream.use { DdsInfoReader.read(it) }
        return DdsMetadata(info.width, info.height, info.format)
    }
}

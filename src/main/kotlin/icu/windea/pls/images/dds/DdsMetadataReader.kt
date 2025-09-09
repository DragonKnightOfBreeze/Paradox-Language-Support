package icu.windea.pls.images.dds

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.images.dds.info.DdsInfoReader

/**
 * @see DdsInfoReader
 */
object DdsMetadataReader {
    fun read(file: VirtualFile): DdsMetadata? {
        return runCatchingCancelable { doRead(file) }.getOrNull()
    }

    private fun doRead(file: VirtualFile): DdsMetadata {
        val info = file.inputStream.use { DdsInfoReader.read(it) }
        return DdsMetadata(info.width, info.height, info.format)
    }
}

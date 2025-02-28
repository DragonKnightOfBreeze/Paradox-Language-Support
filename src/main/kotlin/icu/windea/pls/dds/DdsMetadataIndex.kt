package icu.windea.pls.dds

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.support.*
import java.io.*

//org.intellij.images.index.ImageInfoIndex

object DdsMetadataIndex {
    private val valueExternalizer: DataExternalizer<DdsMetadata> = object : DataExternalizer<DdsMetadata> {
        override fun save(storage: DataOutput, metadata: DdsMetadata) {
            storage.writeIntFast(metadata.width)
            storage.writeIntFast(metadata.height)

        }

        override fun read(storage: DataInput): DdsMetadata {
            val width = DataInputOutputUtil.readINT(storage)
            val height = DataInputOutputUtil.readINT(storage)
            return DdsMetadata(width, height)
        }
    }

    private val gist: VirtualFileGist<DdsMetadata> = GistManager.getInstance().newVirtualFileGist("DdsMetadata", 1, valueExternalizer) { _, file ->
        if (!file.isInLocalFileSystem) return@newVirtualFileGist null
        if (file.fileType != DdsFileType) return@newVirtualFileGist null
        DdsSupport.getMetadata(file)
    }

    fun getMetadata(file: VirtualFile, project: Project): DdsMetadata? {
        return gist.getFileData(project, file)
    }
}

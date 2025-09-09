package icu.windea.pls.images.dds

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.gist.GistManager
import com.intellij.util.gist.VirtualFileGist
import com.intellij.util.io.DataExternalizer
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import java.io.DataInput
import java.io.DataOutput

//org.intellij.images.index.ImageInfoIndex

@Service
class DdsMetadataIndex {
    private val valueExternalizer: DataExternalizer<DdsMetadata> = object : DataExternalizer<DdsMetadata> {
        override fun save(storage: DataOutput, metadata: DdsMetadata) {
            storage.writeIntFast(metadata.width)
            storage.writeIntFast(metadata.height)
            storage.writeUTFFast(metadata.format)
        }

        override fun read(storage: DataInput): DdsMetadata {
            return DdsMetadata(
                width = storage.readIntFast(),
                height = storage.readIntFast(),
                format = storage.readUTFFast(),
            )
        }
    }

    private val gist: VirtualFileGist<DdsMetadata> by lazy {
        GistManager.getInstance().newVirtualFileGist("DdsMetadata", 5, valueExternalizer) c@{ _, file ->
            if (!file.isInLocalFileSystem) return@c null
            if (file.fileType != DdsFileType) return@c null
            if (file.length > Registry.get("ide.index.image.max.size").asDouble() * 1024 * 1024) return@c null
            DdsMetadataReader.read(file)
        }
    }

    fun getMetadata(file: VirtualFile, project: Project): DdsMetadata? {
        return gist.getFileData(project, file)
    }
}

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
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import java.io.DataInput
import java.io.DataOutput
import java.util.*

//org.intellij.images.index.ImageInfoIndex

@Service
class DdsMetadataIndex {
    private val valueExternalizer: DataExternalizer<DdsMetadata> = object : DataExternalizer<DdsMetadata> {
        override fun save(storage: DataOutput, metadata: DdsMetadata) {
            storage.writeIntFast(metadata.width)
            storage.writeIntFast(metadata.height)
            val bitSet = BitSet(8)
            metadata.hasMipMaps?.let { bitSet.set(0) }
            metadata.isFlatTexture?.let { bitSet.set(1) }
            metadata.isCubeMap?.let { bitSet.set(2) }
            metadata.isVolumeTexture?.let { bitSet.set(3) }
            metadata.isDxt10?.let { bitSet.set(4) }
            metadata.d3dFormat?.let { bitSet.set(5) }
            metadata.dxgiFormat?.let { bitSet.set(6) }
            storage.writeByte(bitSet.toByteArray()[0])
            metadata.hasMipMaps?.let { storage.writeBoolean(it) }
            metadata.isFlatTexture?.let { storage.writeBoolean(it) }
            metadata.isCubeMap?.let { storage.writeBoolean(it) }
            metadata.isVolumeTexture?.let { storage.writeBoolean(it) }
            metadata.isDxt10?.let { storage.writeBoolean(it) }
            metadata.d3dFormat?.let { storage.writeUTFFast(it) }
            metadata.dxgiFormat?.let { storage.writeUTFFast(it) }
        }

        override fun read(storage: DataInput): DdsMetadata {
            val width = storage.readIntFast()
            val height = storage.readIntFast()
            val bitSet = BitSet.valueOf(byteArrayOf(storage.readByte()))
            return DdsMetadata(
                width = width,
                height = height,
                hasMipMaps = if (bitSet.get(0)) storage.readBoolean() else null,
                isFlatTexture = if (bitSet.get(1)) storage.readBoolean() else null,
                isCubeMap = if (bitSet.get(2)) storage.readBoolean() else null,
                isVolumeTexture = if (bitSet.get(3)) storage.readBoolean() else null,
                isDxt10 = if (bitSet.get(4)) storage.readBoolean() else null,
                d3dFormat = if (bitSet.get(5)) storage.readUTFFast() else null,
                dxgiFormat = if (bitSet.get(6)) storage.readUTFFast() else null,
            )
        }
    }

    private val gist: VirtualFileGist<DdsMetadata> by lazy {
        GistManager.getInstance().newVirtualFileGist("DdsMetadata", 2, valueExternalizer) c@{ _, file ->
            if (!file.isInLocalFileSystem) return@c null
            if (file.fileType != DdsFileType) return@c null
            if (file.length > Registry.get("ide.index.image.max.size").asDouble() * 1024 * 1024) return@c null
            DdsManager.getMetadata(file)
        }
    }

    fun getMetadata(file: VirtualFile, project: Project): DdsMetadata? {
        return gist.getFileData(project, file)
    }
}

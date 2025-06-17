package icu.windea.pls.images.tga

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.registry.*
import com.intellij.openapi.vfs.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import java.io.*

//org.intellij.images.index.ImageInfoIndex

@Service
class TgaMetadataIndex {
    private val valueExternalizer: DataExternalizer<TgaMetadata> = object : DataExternalizer<TgaMetadata> {
        override fun save(storage: DataOutput, metadata: TgaMetadata) {
            storage.writeIntFast(metadata.width)
            storage.writeIntFast(metadata.height)
            storage.writeIntFast(metadata.bpp)
        }

        override fun read(storage: DataInput): TgaMetadata {
            return TgaMetadata(
                width = storage.readIntFast(),
                height = storage.readIntFast(),
                bpp = storage.readIntFast(),
            )
        }
    }

    private val gist: VirtualFileGist<TgaMetadata> by lazy {
        GistManager.getInstance().newVirtualFileGist("TgaMetadata", 2, valueExternalizer) c@{ _, file ->
            if (!file.isInLocalFileSystem) return@c null
            if (file.fileType != TgaFileType) return@c null
            if (file.length > Registry.get("ide.index.image.max.size").asDouble() * 1024 * 1024) return@c null
            TgaManager.getMetadata(file)
        }
    }

    fun getMetadata(file: VirtualFile, project: Project): TgaMetadata? {
        return gist.getFileData(project, file)
    }
}

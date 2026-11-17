package icu.windea.pls.images.actions

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.tga.TgaFileType
import org.intellij.images.fileTypes.impl.ImageFileType

interface ConvertImageFormatActions {
    class Png : ConvertImageFormatAction("PNG") {
        override fun isAvailableForFile(file: VirtualFile): Boolean {
            if (file.fileType === ImageFileType.INSTANCE && file.extension?.lowercase() == "png") return false
            return super.isAvailableForFile(file)
        }
    }

    class Jpg : ConvertImageFormatAction("JPG") {
        override fun isAvailableForFile(file: VirtualFile): Boolean {
            val extension = file.extension?.lowercase()
            if (extension == "jpg" || extension == "jpeg") return false
            return super.isAvailableForFile(file)
        }
    }

    class Gif : ConvertImageFormatAction("GIF") {
        override fun isAvailableForFile(file: VirtualFile): Boolean {
            if (file.extension?.lowercase() == "gif") return false
            return super.isAvailableForFile(file)
        }
    }

    class Dds : ConvertImageFormatAction("DDS") {
        override fun isAvailableForFile(file: VirtualFile): Boolean {
            if (file.fileType === DdsFileType) return false
            return super.isAvailableForFile(file)
        }
    }

    class Tga : ConvertImageFormatAction("TGA") {
        override fun isAvailableForFile(file: VirtualFile): Boolean {
            if (file.fileType === TgaFileType) return false
            return super.isAvailableForFile(file)
        }
    }
}

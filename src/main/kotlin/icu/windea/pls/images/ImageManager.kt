package icu.windea.pls.images

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.images.dds.*
import icu.windea.pls.images.tga.TgaFileType
import org.intellij.images.fileTypes.impl.*

object ImageManager {
    fun isImageFileType(fileType: FileType): Boolean {
        return fileType == ImageFileType.INSTANCE || isExtendedImageFileType(fileType)
    }

    fun isExtendedImageFileType(fileType: FileType): Boolean {
        if (fileType !is DdsFileType && fileType !is TgaFileType) return false
        return true
    }
}

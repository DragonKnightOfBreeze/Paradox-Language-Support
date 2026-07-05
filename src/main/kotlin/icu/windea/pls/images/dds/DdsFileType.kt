package icu.windea.pls.images.dds

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.UserBinaryFileType
import icu.windea.pls.ChronicleBundle

// org.intellij.images.fileTypes.impl.ImageFileType

object DdsFileType : UserBinaryFileType() {
    override fun getName() = "DDS"

    override fun getDefaultExtension(): String = "dds"

    override fun getDisplayName() = ChronicleBundle.message("filetype.dds.display.name")

    override fun getDescription() = ChronicleBundle.message("filetype.dds.description")

    override fun getIcon() = AllIcons.FileTypes.Image

    val contentTypeString get() = "description=${description};file_extensions=${defaultExtension}"
}

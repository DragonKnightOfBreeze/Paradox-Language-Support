package icu.windea.pls.images.dds

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.UserBinaryFileType
import icu.windea.pls.PlsBundle

//org.intellij.images.fileTypes.impl.ImageFileType

object DdsFileType : UserBinaryFileType() {
    override fun getName() = "DDS"

    override fun getDescription() = PlsBundle.message("filetype.dds.description")

    override fun getDisplayName() = PlsBundle.message("filetype.dds.display.name")

    override fun getIcon() = AllIcons.FileTypes.Image
}

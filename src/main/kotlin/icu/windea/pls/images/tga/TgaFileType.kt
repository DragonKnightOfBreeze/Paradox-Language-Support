package icu.windea.pls.images.tga

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.UserBinaryFileType
import icu.windea.pls.PlsBundle

//org.intellij.images.fileTypes.impl.ImageFileType

object TgaFileType : UserBinaryFileType() {
    override fun getName() = "TGA"

    override fun getDescription() = PlsBundle.message("filetype.tga.description")

    override fun getDisplayName() = PlsBundle.message("filetype.tga.display.name")

    override fun getIcon() = AllIcons.FileTypes.Image
}

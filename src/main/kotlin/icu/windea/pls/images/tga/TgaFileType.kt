package icu.windea.pls.images.tga

import com.intellij.icons.*
import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

//org.intellij.images.fileTypes.impl.ImageFileType

object TgaFileType : UserBinaryFileType() {
    override fun getName() = "TGA"

    override fun getDescription() = PlsBundle.message("filetype.tga.description")

    override fun getDisplayName() = PlsBundle.message("filetype.tga.display.name")

    override fun getIcon() = AllIcons.FileTypes.Image
}

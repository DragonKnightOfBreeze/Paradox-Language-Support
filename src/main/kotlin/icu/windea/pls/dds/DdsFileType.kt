package icu.windea.pls.dds

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*

//org.intellij.images.fileTypes.impl.ImageFileType

object DdsFileType : UserBinaryFileType() {
    override fun getName() = "DDS"
    
    override fun getDescription() = PlsBundle.message("filetype.dds.description")
    
    override fun getDisplayName() = PlsBundle.message("filetype.dds.displayName")
    
    override fun getIcon() = PlsIcons.FileTypes.Dds
}
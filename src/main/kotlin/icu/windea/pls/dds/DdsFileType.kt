package icu.windea.pls.dds

import com.intellij.icons.*
import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

//org.intellij.images.fileTypes.impl.ImageFileType

object DdsFileType : UserBinaryFileType() {
    override fun getName() = "DDS"
    
    override fun getDescription() = PlsBundle.message("filetype.dds.description")
    
    override fun getDisplayName() = PlsBundle.message("filetype.dds.displayName")
    
    override fun getIcon() = AllIcons.FileTypes.Image
}
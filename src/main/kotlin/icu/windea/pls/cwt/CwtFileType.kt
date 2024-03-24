package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

object CwtFileType : LanguageFileType(CwtLanguage) {
    override fun getName() = "Cwt"
    
    override fun getDescription() = PlsBundle.message("filetype.cwt.description")
    
    override fun getDisplayName() = PlsBundle.message("filetype.cwt.displayName")
    
    override fun getDefaultExtension() = "cwt"
    
    override fun getIcon() = PlsIcons.FileTypes.Cwt
}
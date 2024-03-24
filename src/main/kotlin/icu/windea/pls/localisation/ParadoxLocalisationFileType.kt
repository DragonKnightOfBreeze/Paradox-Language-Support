package icu.windea.pls.localisation

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

object ParadoxLocalisationFileType : LanguageFileType(ParadoxLocalisationLanguage) {
    override fun getName() = "Paradox Localisation"
    
    override fun getDescription() = PlsBundle.message("filetype.localisation.description")
    
    override fun getDisplayName() = PlsBundle.message("filetype.localisation.displayName")
    
    override fun getDefaultExtension() = "yml"
    
    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation
}


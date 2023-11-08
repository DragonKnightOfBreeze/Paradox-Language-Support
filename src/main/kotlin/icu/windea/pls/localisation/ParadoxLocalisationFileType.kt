package icu.windea.pls.localisation

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*

object ParadoxLocalisationFileType : LanguageFileType(ParadoxLocalisationLanguage) {
    override fun getName() = "Paradox Localisation"
    
    override fun getDescription() = PlsBundle.message("filetype.localisation.description")
    
    override fun getDisplayName() = PlsBundle.message("filetype.localisation.displayName")
    
    override fun getDefaultExtension() = "yml"
    
    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation
}


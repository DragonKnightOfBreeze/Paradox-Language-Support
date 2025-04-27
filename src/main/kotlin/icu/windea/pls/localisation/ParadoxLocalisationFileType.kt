package icu.windea.pls.localisation

import icu.windea.pls.*
import icu.windea.pls.lang.*

class ParadoxLocalisationFileType : ParadoxBaseFileType(ParadoxLocalisationLanguage.INSTANCE) {
    override fun getName() = "Paradox Localisation"

    override fun getDescription() = PlsBundle.message("filetype.localisation.description")

    override fun getDisplayName() = PlsBundle.message("filetype.localisation.displayName")

    override fun getDefaultExtension() = "yml"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation

    @Suppress("CompanionObjectInExtension")
    companion object {
        @JvmField
        val INSTANCE = ParadoxLocalisationFileType()
    }
}


package icu.windea.pls.localisation

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxLocalisationFileType : ParadoxFileType(ParadoxLocalisationLanguage) {
    override fun getName() = "Paradox Localisation"

    override fun getDisplayName() = ChronicleBundle.message("localisation.fileType.displayName")

    override fun getDescription() = ChronicleBundle.message("localisation.fileType.description")

    override fun getDefaultExtension() = "yml"

    override fun getIcon() = ChronicleIcons.FileTypes.ParadoxLocalisation
}

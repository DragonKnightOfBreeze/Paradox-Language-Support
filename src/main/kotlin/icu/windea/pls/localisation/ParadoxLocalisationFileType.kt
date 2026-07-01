package icu.windea.pls.localisation

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxLocalisationFileType : ParadoxFileType(ParadoxLocalisationLanguage) {
    override fun getName() = "Paradox Localisation"

    override fun getDisplayName() = ChronicleBundle.message("localisation.language.name")

    override fun getDescription() = ChronicleBundle.message("localisation.settings.name")

    override fun getDefaultExtension() = "yml"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation
}

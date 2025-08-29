package icu.windea.pls.localisation

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.ParadoxBaseFileType

object ParadoxLocalisationFileType : ParadoxBaseFileType(ParadoxLocalisationLanguage) {
    override fun getName() = "Paradox Localisation"

    override fun getDescription() = PlsBundle.message("language.name.localisation")

    override fun getDefaultExtension() = "yml"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation
}

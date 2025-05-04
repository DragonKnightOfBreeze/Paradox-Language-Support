package icu.windea.pls.localisation

import icu.windea.pls.*
import icu.windea.pls.lang.*

object ParadoxLocalisationFileType : ParadoxBaseFileType(ParadoxLocalisationLanguage) {
    override fun getName() = "Paradox Localisation"

    override fun getDescription() = PlsBundle.message("language.name.localisation")

    override fun getDefaultExtension() = "yml"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation
}

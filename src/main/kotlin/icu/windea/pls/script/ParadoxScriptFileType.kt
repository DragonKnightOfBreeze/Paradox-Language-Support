package icu.windea.pls.script

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxScriptFileType : ParadoxFileType(ParadoxScriptLanguage) {
    override fun getName() = "Paradox Script"

    override fun getDisplayName() = PlsBundle.message("script.language.name")

    override fun getDescription() = PlsBundle.message("script.settings.name")

    override fun getDefaultExtension() = "txt"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript
}

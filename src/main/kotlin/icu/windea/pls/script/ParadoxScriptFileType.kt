package icu.windea.pls.script

import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.ParadoxBaseFileType

object ParadoxScriptFileType : ParadoxBaseFileType(ParadoxScriptLanguage) {
    override fun getName() = "Paradox Script"

    override fun getDescription() = PlsBundle.message("language.name.script")

    override fun getDefaultExtension() = "txt"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript
}

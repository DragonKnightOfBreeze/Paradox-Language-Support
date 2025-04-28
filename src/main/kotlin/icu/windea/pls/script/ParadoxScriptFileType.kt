package icu.windea.pls.script

import icu.windea.pls.*
import icu.windea.pls.lang.*

object ParadoxScriptFileType : ParadoxBaseFileType(ParadoxScriptLanguage) {
    override fun getName() = "Paradox Script"

    override fun getDescription() = PlsBundle.message("language.name.script")

    override fun getDefaultExtension() = "txt"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript
}

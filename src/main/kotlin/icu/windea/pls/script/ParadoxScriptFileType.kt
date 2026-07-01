package icu.windea.pls.script

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxScriptFileType : ParadoxFileType(ParadoxScriptLanguage) {
    override fun getName() = "Paradox Script"

    override fun getDisplayName() = ChronicleBundle.message("script.language.name")

    override fun getDescription() = ChronicleBundle.message("script.settings.name")

    override fun getDefaultExtension() = "txt"

    override fun getIcon() = ChronicleIcons.FileTypes.ParadoxScript
}

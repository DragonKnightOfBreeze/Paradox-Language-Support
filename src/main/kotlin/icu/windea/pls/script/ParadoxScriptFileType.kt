package icu.windea.pls.script

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.ParadoxFileType

object ParadoxScriptFileType : ParadoxFileType(ParadoxScriptLanguage) {
    override fun getName() = "Paradox Script"

    override fun getDisplayName() = ChronicleBundle.message("script.fileType.displayName")

    override fun getDescription() = ChronicleBundle.message("script.fileType.description")

    override fun getDefaultExtension() = "txt"

    override fun getIcon() = ChronicleIcons.FileTypes.ParadoxScript
}

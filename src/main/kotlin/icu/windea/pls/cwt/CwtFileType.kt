package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.LanguageFileType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.PlsIcons

object CwtFileType : LanguageFileType(CwtLanguage) {
    override fun getName() = "Cwt"

    override fun getDisplayName() = ChronicleBundle.message("cwt.language.name")

    override fun getDescription() = ChronicleBundle.message("cwt.settings.name")

    override fun getDefaultExtension() = "cwt"

    override fun getIcon() = PlsIcons.FileTypes.Cwt
}

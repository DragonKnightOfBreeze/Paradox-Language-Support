package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.LanguageFileType
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons

object CwtFileType : LanguageFileType(CwtLanguage) {
    override fun getName() = "Cwt"

    override fun getDescription() = PlsBundle.message("language.name.cwt")

    override fun getDefaultExtension() = "cwt"

    override fun getIcon() = PlsIcons.FileTypes.Cwt
}

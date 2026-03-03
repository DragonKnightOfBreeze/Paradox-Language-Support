package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.LanguageFileType
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons

object CwtFileType : LanguageFileType(CwtLanguage) {
    override fun getName() = "Cwt"

    override fun getDisplayName() = PlsBundle.message("cwt.language.name")

    override fun getDescription() = PlsBundle.message("cwt.settings.name")

    override fun getDefaultExtension() = "cwt"

    override fun getIcon() = PlsIcons.FileTypes.Cwt
}

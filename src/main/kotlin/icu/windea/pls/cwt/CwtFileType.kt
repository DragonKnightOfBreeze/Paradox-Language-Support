package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

class CwtFileType : LanguageFileType(CwtLanguage.INSTANCE) {
    override fun getName() = "Cwt"

    override fun getDescription() = PlsBundle.message("filetype.cwt.description")

    override fun getDisplayName() = PlsBundle.message("filetype.cwt.displayName")

    override fun getDefaultExtension() = "cwt"

    override fun getIcon() = PlsIcons.FileTypes.Cwt

    @Suppress("CompanionObjectInExtension")
    companion object {
        @JvmField
        val INSTANCE = CwtFileType()
    }
}

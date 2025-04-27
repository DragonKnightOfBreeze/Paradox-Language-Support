package icu.windea.pls.script

import icu.windea.pls.*
import icu.windea.pls.lang.*

class ParadoxScriptFileType : ParadoxBaseFileType(ParadoxScriptLanguage.INSTANCE) {
    override fun getName() = "Paradox Script"

    override fun getDescription() = PlsBundle.message("filetype.script.description")

    override fun getDisplayName() = PlsBundle.message("filetype.script.displayName")

    override fun getDefaultExtension() = "txt"

    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript

    @Suppress("CompanionObjectInExtension")
    companion object {
        @JvmField
        val INSTANCE = ParadoxScriptFileType()
    }
}

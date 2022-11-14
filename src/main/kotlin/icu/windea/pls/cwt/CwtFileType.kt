package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*

object CwtFileType : LanguageFileType(CwtLanguage) {
	override fun getName() = "Cwt"
	
	override fun getDescription() = PlsBundle.message("filetype.cwt.description")
	
	override fun getDisplayName() = PlsBundle.message("filetype.cwt.displayName")
	
	override fun getDefaultExtension() = "cwt"
	
	override fun getIcon() = PlsIcons.CwtFile
}
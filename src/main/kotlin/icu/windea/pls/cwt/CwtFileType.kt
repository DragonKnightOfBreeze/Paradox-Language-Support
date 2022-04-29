package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

object CwtFileType : LanguageFileType(CwtLanguage) {
	override fun getName() = cwtName
	
	override fun getDescription() = cwtDescription
	
	override fun getDefaultExtension() = cwtExtension
	
	override fun getIcon() = PlsIcons.cwtFileIcon
}
package icu.windea.pls.cwt

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*
import javax.swing.*

object CwtFileType: LanguageFileType(CwtLanguage){
	override fun getName() = cwtFileTypeName
	
	override fun getDescription() = cwtFileTypeDescription
	
	override fun getDefaultExtension() = cwtExtension
	
	override fun getIcon() = cwtFileIcon
}
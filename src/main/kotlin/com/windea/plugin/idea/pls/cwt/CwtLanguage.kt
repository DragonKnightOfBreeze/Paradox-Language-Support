package com.windea.plugin.idea.pls.cwt

import com.intellij.lang.*
import com.intellij.openapi.fileTypes.*
import com.windea.plugin.idea.pls.*
import javax.swing.*

object CwtLanguage: Language(cwtName)

object CwtFileType: LanguageFileType(CwtLanguage){
	override fun getName() = cwtFileTypeName
	
	override fun getDescription() = cwtFileTypeDescription
	
	override fun getDefaultExtension() = cwtExtension
	
	override fun getIcon(): Icon? {
		return cwtFileIcon
	}
}
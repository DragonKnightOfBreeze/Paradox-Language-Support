package icu.windea.pls.script

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

object ParadoxScriptFileType : LanguageFileType(ParadoxScriptLanguage) {
	override fun getName() = paradoxScriptName
	
	override fun getDescription() = paradoxScriptDescription
	
	override fun getDefaultExtension() = paradoxScriptExtension
	
	override fun getIcon() = PlsIcons.paradoxScriptFileIcon
}

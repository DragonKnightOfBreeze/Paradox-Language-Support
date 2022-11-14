package icu.windea.pls.script

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*

object ParadoxScriptFileType : LanguageFileType(ParadoxScriptLanguage) {
	override fun getName() = "Paradox Script"
	
	override fun getDescription() = PlsBundle.message("filetype.script.description")
	
	override fun getDisplayName() = PlsBundle.message("filetype.script.displayName")
	
	override fun getDefaultExtension() = "txt"
	
	override fun getIcon() = PlsIcons.ParadoxScriptFile
}

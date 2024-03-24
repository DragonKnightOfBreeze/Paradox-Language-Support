package icu.windea.pls.script

import com.intellij.openapi.fileTypes.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

object ParadoxScriptFileType : LanguageFileType(ParadoxScriptLanguage) {
	override fun getName() = "Paradox Script"
	
	override fun getDescription() = PlsBundle.message("filetype.script.description")
	
	override fun getDisplayName() = PlsBundle.message("filetype.script.displayName")
	
	override fun getDefaultExtension() = "txt"
	
	override fun getIcon() = PlsIcons.FileTypes.ParadoxScript
}

package icu.windea.pls.expression

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

object ParadoxExpressionFileType : LanguageFileType(ParadoxExpressionLanguage) {
	override fun getName() = "Paradox Script"
	
	override fun getDescription() = PlsBundle.message("filetype.expression.description")
	
	override fun getDisplayName() = PlsBundle.message("filetype.expression.displayName")
	
	override fun getDefaultExtension() = ""
	
	override fun getIcon() = null
}
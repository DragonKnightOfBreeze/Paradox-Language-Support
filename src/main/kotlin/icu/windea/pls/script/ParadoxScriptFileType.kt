package icu.windea.pls.script

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

object ParadoxScriptFileType : LanguageFileType(ParadoxScriptLanguage) {
	override fun getName() = paradoxScriptFileTypeName

	override fun getDescription() = paradoxScriptFileTypeDescription

	override fun getDefaultExtension() = paradoxScriptExtension

	override fun getIcon() = paradoxScriptFileIcon
}

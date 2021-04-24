package com.windea.plugin.idea.pls.script

import com.intellij.openapi.fileTypes.*
import com.windea.plugin.idea.pls.*

object ParadoxScriptFileType : LanguageFileType(ParadoxScriptLanguage) {
	override fun getName() = paradoxScriptFileTypeName

	override fun getDescription() = paradoxScriptFileTypeDescription

	override fun getDefaultExtension() = paradoxScriptExtension

	override fun getIcon() = paradoxScriptFileIcon
}

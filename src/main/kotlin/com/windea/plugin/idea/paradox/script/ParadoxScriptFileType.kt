package com.windea.plugin.idea.paradox.script

import com.intellij.openapi.fileTypes.*
import com.windea.plugin.idea.paradox.*

object ParadoxScriptFileType : LanguageFileType(ParadoxScriptLanguage) {
	override fun getName() = paradoxScriptFileTypeName

	override fun getDescription() = paradoxScriptFileTypeDescription

	override fun getDefaultExtension() = paradoxScriptExtension

	override fun getIcon() = paradoxScriptFileIcon
}

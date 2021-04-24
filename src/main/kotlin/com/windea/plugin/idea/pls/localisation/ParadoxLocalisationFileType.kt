package com.windea.plugin.idea.pls.localisation

import com.intellij.openapi.fileTypes.*
import com.windea.plugin.idea.pls.*

object ParadoxLocalisationFileType : LanguageFileType(ParadoxLocalisationLanguage) {
	override fun getName() = paradoxLocalisationFileTypeName

	override fun getDescription() = paradoxLocalisationFileTypeDescription

	override fun getDefaultExtension() = paradoxLocalisationExtension

	override fun getIcon() = paradoxLocalisationFileIcon
}


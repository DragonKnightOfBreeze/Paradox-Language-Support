package com.windea.plugin.idea.paradox.localisation

import com.intellij.openapi.fileTypes.*
import com.windea.plugin.idea.paradox.*

object ParadoxLocalisationFileType : LanguageFileType(ParadoxLocalisationLanguage) {
	override fun getName() = paradoxLocalisationFileTypeName

	override fun getDescription() = paradoxLocalisationFileTypeDescription

	override fun getDefaultExtension() = paradoxLocalisationExtension

	override fun getIcon() = paradoxLocalisationFileIcon
}


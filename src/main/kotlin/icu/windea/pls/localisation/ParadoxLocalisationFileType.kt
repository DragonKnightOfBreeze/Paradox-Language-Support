package icu.windea.pls.localisation

import com.intellij.openapi.fileTypes.*
import icu.windea.pls.*

object ParadoxLocalisationFileType : LanguageFileType(ParadoxLocalisationLanguage) {
	override fun getName() = paradoxLocalisationName

	override fun getDescription() = paradoxLocalisationDescription

	override fun getDefaultExtension() = paradoxLocalisationExtension

	override fun getIcon() = paradoxLocalisationFileIcon
}


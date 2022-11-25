package icu.windea.pls.core.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*

enum class ParadoxFileType(
	val id: String,
	val description: String
)  {
	Directory("directory", "Directory"),
	ParadoxScript("paradoxScript", "Paradox Script File"),
	ParadoxLocalisation("paradoxLocalisation", "Paradox Localisation File"),
	Dds("dds", "Dds File"),
	Other("other", "Other File");
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		fun resolve(file: VirtualFile, gameType: ParadoxGameType, path: ParadoxPath): ParadoxFileType {
			if(file.isDirectory) return Directory
			val fileName = file.name
			val fileExtension = file.extension?.lowercase() ?: return Other
			return when {
				fileName == PlsConstants.descriptorFileName -> ParadoxScript
				path.canBeScriptFilePath() && fileExtension in PlsConstants.scriptFileExtensions && !isIgnored(fileName) && isInFolders(gameType, path) -> ParadoxScript
				path.canBeLocalisationFilePath() && fileExtension in PlsConstants.localisationFileExtensions && !isIgnored(fileName) -> ParadoxLocalisation
				fileExtension in PlsConstants.ddsFileExtensions -> Dds
				else -> Other
			}
		}
		
		private fun isIgnored(fileName: String): Boolean {
			return getSettings().ignoredFileNames.commaDelimited().contains(fileName, ignoreCase = true)
		}
		
		private fun isInFolders(gameType: ParadoxGameType, path: ParadoxPath): Boolean {
			if(path.parent.isEmpty()) return false
			val folders = getCwtConfig(getDefaultProject()).get(gameType)?.folders
			return folders.isNullOrEmpty() || folders.any { it.matchesPath(path.parent) }
		}
	}
}

package icu.windea.pls.core.model

import com.intellij.openapi.vfs.*
import icu.windea.pls.*

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
				fileName == descriptorFileName -> ParadoxScript
				path.canBeScriptFilePath() && fileExtension in scriptFileExtensions && !isIgnored(fileName) && isInFolders(gameType, path) -> ParadoxScript
				path.canBeLocalisationFilePath() && fileExtension in localisationFileExtensions -> ParadoxLocalisation
				fileExtension in ddsFileExtensions -> Dds
				else -> Other
			}
		}
		
		private fun isIgnored(fileName: String): Boolean {
			return getSettings().finalScriptIgnoredFileNames.contains(fileName)
		}
		
		private fun isInFolders(gameType: ParadoxGameType, path: ParadoxPath): Boolean {
			if(path.parent.isEmpty()) return false
			val folders = getCwtConfig(getDefaultProject()).get(gameType)?.folders
			return folders.isNullOrEmpty() || folders.any { it.matchesPath(path.parent) }
		}
	}
}
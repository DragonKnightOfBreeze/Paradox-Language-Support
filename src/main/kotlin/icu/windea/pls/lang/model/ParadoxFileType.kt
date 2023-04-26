package icu.windea.pls.lang.model

import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*

enum class ParadoxFileType(
	val id: String,
	val description: String
)  {
	Directory("directory", "Directory"),
	ParadoxScript("paradoxScript", "Paradox Script File"),
	ParadoxLocalisation("paradoxLocalisation", "Paradox Localisation File"),
	Other("other", "Other File");
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		fun resolve(file: VirtualFile, path: ParadoxPath): ParadoxFileType {
			if(file.isDirectory) return Directory
			val fileName = file.name
			val fileExtension = file.extension?.lowercase() ?: return Other
			return when {
				fileName.equals(PlsConstants.descriptorFileName, true) -> ParadoxScript
				path.canBeScriptFilePath() && fileExtension in PlsConstants.scriptFileExtensions && !isIgnored(fileName) -> ParadoxScript
				path.canBeLocalisationFilePath() && fileExtension in PlsConstants.localisationFileExtensions && !isIgnored(fileName) -> ParadoxLocalisation
				else -> Other
			}
		}
		
		fun resolve(filePath: FilePath, path: ParadoxPath): ParadoxFileType {
			if(filePath.isDirectory) return Directory
			val fileName = filePath.name
			val fileExtension = fileName.substringAfterLast('.', "").lowercase().takeIfNotEmpty() ?: return Other
			return when {
				fileName.equals(PlsConstants.descriptorFileName, true) -> ParadoxScript
				path.canBeScriptFilePath() && fileExtension in PlsConstants.scriptFileExtensions && !isIgnored(fileName) -> ParadoxScript
				path.canBeLocalisationFilePath() && fileExtension in PlsConstants.localisationFileExtensions && !isIgnored(fileName) -> ParadoxLocalisation
				else -> Other
			}
		}
		
		private fun isIgnored(fileName: String): Boolean {
			return getSettings().ignoredFileNameSet.contains(fileName.lowercase())
		}
		
		//忽略CWT规则文件folders.cwt
		//private fun isInFolders(gameType: ParadoxGameType, path: ParadoxPath): Boolean {
		//	if(path.parent.isEmpty()) return false
		//	val folders = getCwtConfig(getDefaultProject()).get(gameType)?.folders
		//	return folders.isNullOrEmpty() || folders.any { it.matchesPath(path.parent) }
		//}
	}
}

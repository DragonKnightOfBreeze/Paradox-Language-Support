package icu.windea.pls.model

import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.path.*

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
		fun resolve(file: VirtualFile, path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
			if(file.isDirectory) return Directory
			return doResolve(path, rootInfo)
		}
		
		fun resolve(filePath: FilePath, path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
			if(filePath.isDirectory) return Directory
			return doResolve(path, rootInfo)
		}
		
		private fun doResolve(path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
			val fileName = path.fileName.lowercase()
			val fileExtension = path.fileExtension?.lowercase() ?: return Other
			return when {
				fileName == PlsConstants.descriptorFileName -> ParadoxScript
				path.length == 1 && rootInfo is ParadoxGameRootInfo -> Other
				path.canBeScriptFilePath() && fileExtension in PlsConstants.scriptFileExtensions && !isIgnored(fileName) -> ParadoxScript
				path.canBeLocalisationFilePath() && fileExtension in PlsConstants.localisationFileExtensions && !isIgnored(fileName) -> ParadoxLocalisation
				else -> Other
			}
		}
		
		private fun isIgnored(fileName: String): Boolean {
			return getSettings().ignoredFileNameSet.contains(fileName)
		}
		
		//NOTE PLS use its own logic to resolve actual file type, so folders.cwt will be ignored
		//private fun isInFolders(gameType: ParadoxGameType, path: ParadoxPath): Boolean {
		//	if(path.parent.isEmpty()) return false
		//	val folders = getConfigGroup(getDefaultProject(), gameType)?.folders
		//	return folders.isNullOrEmpty() || folders.any { it.matchesPath(path.parent) }
		//}
	}
}

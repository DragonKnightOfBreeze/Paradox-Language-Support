package icu.windea.pls.model

import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.lang.util.*

enum class ParadoxFileType {
    Script,
    Localisation,
    Csv,
    ModDescriptor,
    Other,
    ;

    companion object {
        @JvmStatic
        fun resolve(path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
            return doResolve(path, rootInfo)
        }

        @JvmStatic
        fun resolve(file: VirtualFile, path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
            if (file.isDirectory) return Other
            return doResolve(path, rootInfo)
        }

        @JvmStatic
        fun resolve(filePath: FilePath, path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
            if (filePath.isDirectory) return Other
            return doResolve(path, rootInfo)
        }

        private fun doResolve(path: ParadoxPath, rootInfo: ParadoxRootInfo): ParadoxFileType {
            val fileName = path.fileName.lowercase()
            return when {
                fileName.endsWith(".mod") -> ModDescriptor
                path.length == 1 && rootInfo is ParadoxRootInfo.Game -> Other
                isIgnored(fileName) -> Other
                ParadoxFileManager.canBeScriptFilePath(path) -> Script
                ParadoxFileManager.canBeLocalisationFilePath(path) -> Localisation
                ParadoxFileManager.canBeCsvFilePath(path) -> Csv
                else -> Other
            }
        }

        private fun isIgnored(fileName: String): Boolean {
            return PlsFacade.getSettings().ignoredFileNameSet.contains(fileName)
        }

        //NOTE PLS use its own logic to resolve actual file type, so folders.cwt will be ignored
    }
}

package icu.windea.pls.model

import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

enum class ParadoxFileType {
    Script,
    Localisation,
    ModDescriptor,
    Other,
    ;

    companion object {
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
                fileName == PlsConstants.modDescriptorFileName -> ModDescriptor
                path.length == 1 && rootInfo is ParadoxRootInfo.Game -> Other
                isIgnored(fileName) -> Other
                ParadoxFilePathManager.canBeScriptFilePath(path) -> Script
                ParadoxFilePathManager.canBeLocalisationFilePath(path) -> Localisation
                else -> Other
            }
        }

        private fun isIgnored(fileName: String): Boolean {
            return getSettings().ignoredFileNameSet.contains(fileName)
        }

        //NOTE PLS use its own logic to resolve actual file type, so folders.cwt will be ignored
    }
}

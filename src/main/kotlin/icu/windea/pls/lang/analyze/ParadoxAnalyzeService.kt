package icu.windea.pls.lang.analyze

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.core.trimFast
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.analyze.ParadoxInferredGameTypeProvider
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath

object ParadoxAnalyzeService {
    fun resolveRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        val metadata = ParadoxMetadataService.getMetadata(rootFile) ?: return null
        val rootInfo = when (metadata) {
            is ParadoxMetadata.Game -> ParadoxRootInfo.Game(metadata)
            is ParadoxMetadata.Mod -> ParadoxRootInfo.Mod(metadata)
        }
        return rootInfo
    }

    fun resolveFileInfo(file: VirtualFile, rootInfo: ParadoxRootInfo): ParadoxFileInfo? {
        val isDirectory = file.isDirectory
        val (path, entryName) = resolvePathAndEntryName(file.path, isDirectory, rootInfo) ?: return null
        val fileType = when {
            isDirectory -> ParadoxFileType.Other
            path.length == 1 && rootInfo is ParadoxRootInfo.Game -> ParadoxFileType.Other
            ParadoxFileManager.isIgnoredFile(file.name) -> ParadoxFileType.Other
            else -> ParadoxFileType.resolve(path)
        }
        val fileInfo = ParadoxFileInfo(path.normalize(), entryName, fileType, rootInfo)
        return fileInfo
    }

    fun resolveFileInfo(filePath: FilePath, rootInfo: ParadoxRootInfo): ParadoxFileInfo? {
        val isDirectory = filePath.isDirectory
        val (path, entryName) = resolvePathAndEntryName(filePath.path, isDirectory, rootInfo) ?: return null
        val fileType = when {
            isDirectory -> ParadoxFileType.Other
            path.length == 1 && rootInfo is ParadoxRootInfo.Game -> ParadoxFileType.Other
            ParadoxFileManager.isIgnoredFile(filePath.name) -> ParadoxFileType.Other
            else -> ParadoxFileType.resolve(path)
        }
        val fileInfo = ParadoxFileInfo(path.normalize(), entryName, fileType, rootInfo)
        return fileInfo
    }

    private fun resolvePathAndEntryName(filePath: String, isDirectory: Boolean, rootInfo: ParadoxRootInfo): Tuple2<ParadoxPath, String>? {
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val relPath = ParadoxPath.resolve(filePath.removePrefix(rootInfo.rootFile.path).trimFast('/'))
        val entryInfo = rootInfo.gameType.entryInfo
        val entryMap = when (rootInfo) {
            is ParadoxRootInfo.Game -> entryInfo.gameEntryMap
            is ParadoxRootInfo.Mod -> entryInfo.modEntryMap
        }
        if (entryMap.isEmpty()) return relPath to ""
        for ((entryName, entryPath) in entryMap) {
            val resolved = relPath.subPaths.removePrefixOrNull(entryPath, wildcard = "*") ?: continue
            return ParadoxPath.resolve(resolved) to entryName
        }
        if (isDirectory) return relPath to "" // 2.0.7 directories without a matched entry are allowed
        if (filePath == rootInfo.infoFile?.path) return relPath to "" // 2.0.7 info files (e.g., `descriptor.mod`) are allowed
        return null // 2.0.7 null now
    }

    fun resolveLocaleConfig(file: VirtualFile, project: Project): CwtLocaleConfig? {
        val indexId = PlsIndexKeys.FileLocale
        val localeId = FileBasedIndex.getInstance().getFileData(indexId, file, project).keys.singleOrNull() ?: return null
        val localeConfig = PlsFacade.getConfigGroup().localisationLocalesById.get(localeId)
        return localeConfig
    }

    /**
     * @see ParadoxInferredGameTypeProvider.getGameType
     */
    fun getInferredGameType(rootFile: VirtualFile): ParadoxGameType? {
        return ParadoxInferredGameTypeProvider.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.getGameType(rootFile)
        }
    }
}

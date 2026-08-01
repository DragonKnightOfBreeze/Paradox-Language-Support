package icu.windea.pls.lang.analysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.orNull
import icu.windea.pls.core.trimFast
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.analysis.ParadoxIgnoredFileProvider
import icu.windea.pls.ep.analysis.ParadoxInferredGameTypeProvider
import icu.windea.pls.ep.analysis.ParadoxRootMetadataProvider
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameTypeInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.analysis.ParadoxRootMetadata
import icu.windea.pls.model.paths.ParadoxPath
import java.nio.file.Path
import kotlin.io.path.isDirectory

@Optimized
object ParadoxAnalysisService {
    /**
     * @see ParadoxIgnoredFileProvider.isIgnoredFile
     */
    @Optimized
    fun isIgnoredFile(path: ParadoxPath, entry: String): Boolean {
        val eps = ParadoxIgnoredFileProvider.EP_NAME.extensionList
        eps.forEachFast { ep ->
            if (ep.isIgnoredFile(path, entry)) return true
        }
        return false
    }

    /**
     * @see ParadoxRootMetadataProvider.getRootMetadata
     */
    fun getRootMetadata(rootPath: Path): ParadoxRootMetadata? {
        if (!rootPath.isDirectory()) return null
        val eps = ParadoxRootMetadataProvider.EP_NAME.extensionList
        eps.forEachFast { ep ->
            ep.getRootMetadata(rootPath)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxInferredGameTypeProvider.getInferredGameTypeInfo
     */
    fun getInferredGameTypeInfo(rootPath: Path): ParadoxGameTypeInfo? {
        if (!rootPath.isDirectory()) return null
        val eps = ParadoxInferredGameTypeProvider.EP_NAME.extensionList
        eps.forEachFast { ep ->
            ep.getInferredGameTypeInfo(rootPath)?.let { return it }
        }
        return null
    }

    fun resolveRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        // NOTE 2.1.7 invalid metadata is allowed here
        val rootPath = rootFile.toNioPathOrNull() ?: return null
        val metadata = getRootMetadata(rootPath) ?: return null
        val rootInfo = when (metadata) {
            is ParadoxRootMetadata.Game -> ParadoxRootInfo.Game(rootFile, metadata)
            is ParadoxRootMetadata.Mod -> ParadoxRootInfo.Mod(rootFile, metadata)
        }
        return rootInfo
    }

    fun resolveFileInfo(file: VirtualFile, rootInfo: ParadoxRootInfo): ParadoxFileInfo? {
        val isDirectory = file.isDirectory
        val (path, entry) = resolvePathAndEntry(file.path, isDirectory, rootInfo) ?: return null
        val group = when {
            isDirectory -> ParadoxFileGroup.Other
            isIgnoredFile(path, entry) -> ParadoxFileGroup.Other
            else -> ParadoxFileGroup.resolve(path)
        }
        val fileInfo = ParadoxFileInfo(path.normalize(), entry, group, rootInfo)
        return fileInfo
    }

    fun resolveFileInfo(filePath: FilePath, rootInfo: ParadoxRootInfo): ParadoxFileInfo? {
        val isDirectory = filePath.isDirectory
        val (path, entry) = resolvePathAndEntry(filePath.path, isDirectory, rootInfo) ?: return null
        val group = when {
            isDirectory -> ParadoxFileGroup.Other
            isIgnoredFile(path, entry) -> ParadoxFileGroup.Other
            else -> ParadoxFileGroup.resolve(path)
        }
        val fileInfo = ParadoxFileInfo(path.normalize(), entry, group, rootInfo)
        return fileInfo
    }

    private fun resolvePathAndEntry(filePath: String, isDirectory: Boolean, rootInfo: ParadoxRootInfo): Tuple2<ParadoxPath, String>? {
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val pathToRoot = ParadoxPath.resolve(filePath.removePrefix(rootInfo.rootFile.path).trimFast('/'))
        val gameType = rootInfo.gameType
        val entryPaths = when (rootInfo) {
            is ParadoxRootInfo.Game -> gameType.metadata.gameEntryPaths
            is ParadoxRootInfo.Mod -> gameType.metadata.modEntryPaths
        }
        if (entryPaths.isEmpty()) return pathToRoot to ""

        for (entryPath in entryPaths) {
            val resolved = entryPath.relativize(pathToRoot, wildcard = "*") ?: continue
            return resolved to entryPath.path
        }

        // 2.0.7 directories are allowed outside entry paths
        if (isDirectory) return pathToRoot to ""
        // 2.0.7 info files (e.g., `descriptor.mod`) are allowed outside entry paths
        if (pathToRoot.path == rootInfo.metadata.infoPresentablePath) return pathToRoot to ""
        // 2.0.7 null now
        return null
    }

    fun resolveLocaleId(file: VirtualFile, project: Project): String? {
        if (file.fileType != ParadoxLocalisationFileType) return null // fast return (meaningless for non-loc file types)
        val indexId = ChronicleIndexKeys.FileLocale
        val localeId = FileBasedIndex.getInstance().getFileData(indexId, file, project).keys.singleOrNull()
        return localeId?.orNull()
    }
}

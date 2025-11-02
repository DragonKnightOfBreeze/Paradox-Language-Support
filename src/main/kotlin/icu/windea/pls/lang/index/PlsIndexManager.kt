package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findFileBasedIndex
import icu.windea.pls.core.util.createKey
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.index.ParadoxIndexInfo

object PlsIndexManager {
    private val excludeDirectoriesForFilePathIndex = listOf(
        "_CommonRedist",
        "crash_reporter",
        "curated_save_games",
        "pdx_browser",
        "pdx_launcher",
        "pdx_online_assets",
        "previewer_assets",
        "tweakergui_assets",
        "jomini",
    )

    fun includeForFilePathIndex(file: VirtualFile): Boolean {
        if (file.fileInfo == null) return false
        val parent = file.parent
        if (parent != null && parent.fileInfo != null && !includeForFilePathIndex(parent)) return false
        val fileName = file.name
        if (fileName.startsWith('.')) return false // 排除隐藏目录或文件
        if (file.isDirectory) {
            if (fileName in excludeDirectoriesForFilePathIndex) return false // 排除一些特定的目录
            return true
        }
        val fileExtension = fileName.substringAfterLast('.')
        if (fileExtension.isEmpty()) return false
        return fileExtension in PlsConstants.scriptFileExtensions
            || fileExtension in PlsConstants.localisationFileExtensions
            || fileExtension in PlsConstants.csvFileExtensions
            || fileExtension in PlsConstants.imageFileExtensions
    }

    val indexInfoMarkerKey = createKey<Boolean>("paradox.index.info.marker")

    fun processFiles(
        fileType: FileType,
        scope: GlobalSearchScope,
        processor: Processor<VirtualFile>
    ): Boolean {
        ProgressManager.checkCanceled()
        if (SearchScope.isEmptyScope(scope)) return true
        return FileTypeIndex.processFiles(fileType, processor, scope)
    }

    fun <K, V> processFilesWithKeys(
        indexId: ID<K, V>,
        keys: Collection<K>,
        scope: GlobalSearchScope,
        processor: Processor<in VirtualFile>,
    ): Boolean {
        ProgressManager.checkCanceled()
        if (SearchScope.isEmptyScope(scope)) return true
        val finalKeys = getKeysOrAllKeys(indexId, keys, scope)
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(indexId, finalKeys, scope, null, null, processor)
    }

    private fun <K, V> getKeysOrAllKeys(indexId: ID<K, V>, keys: Collection<K>, scope: GlobalSearchScope): Collection<K> {
        if (keys.isNotEmpty()) return keys
        val project = scope.project ?: return emptySet()
        return FileBasedIndex.getInstance().getAllKeys(indexId, project)
    }

    fun <K, V> getFileData(
        indexId: ID<K, V>,
        file: VirtualFile,
        project: Project
    ): Map<K, V> {
        ProgressManager.checkCanceled()
        return FileBasedIndex.getInstance().getFileData(indexId, file, project)
    }

    fun <T : ParadoxIndexInfo> processFiles(
        type: ParadoxIndexInfoType<T>,
        fileType: FileType,
        project: Project,
        gameType: ParadoxGameType,
        scope: GlobalSearchScope,
        processor: (file: VirtualFile, fileData: List<T>) -> Boolean
    ): Boolean {
        ProgressManager.checkCanceled()
        if (SearchScope.isEmptyScope(scope)) return true
        val index = findFileBasedIndex<ParadoxMergedIndex>()
        return FileTypeIndex.processFiles(fileType, p@{ file ->
            ParadoxCoreManager.getFileInfo(file) // ensure file info is resolved here
            if (gameType != selectGameType(file)) return@p true // check game type at file level

            val fileData = index.getFileData(file, project)
            val infos = fileData.get(type.id.toString())?.castOrNull<List<T>>().orEmpty()
            if (infos.isEmpty()) return@p true
            processor(file, infos)
        }, scope)
    }
}

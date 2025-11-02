package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findFileBasedIndex
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo

object PlsIndexService {
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
        return processFiles(fileType, scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxCoreManager.getFileInfo(file) // ensure file info is resolved here
            if (gameType != selectGameType(file)) return@p true // check game type at file level

            val fileData = index.getFileData(file, project)
            val infos = fileData.get(type.id.toString())?.castOrNull<List<T>>().orEmpty()
            if (infos.isEmpty()) return@p true
            processor(file, infos)
        }
    }

    /**
     * 遍历给定键 [key] 下的所有元素并用 [processor] 处理。
     *
     * Dumb 模式下直接返回 `true`。
     */
    inline fun <K : Any, reified T : PsiElement> processElements(
        indexKey: StubIndexKey<K, T>,
        key: K,
        project: Project,
        scope: GlobalSearchScope,
        crossinline processor: (T) -> Boolean
    ): Boolean {
        if (DumbService.isDumb(project)) return true
        return StubIndex.getInstance().processElements(indexKey, key, project, scope, T::class.java) { element ->
            ProgressManager.checkCanceled()
            processor(element)
        }
    }

    /**
     * 遍历所有键，并对命中的键（满足 [keyPredicate]）下的元素调用 [processor]。
     *
     * 提供按键粒度的过滤与处理能力。
     *
     * Dumb 模式下直接返回 `true`。
     */
    inline fun <K : Any, reified T : PsiElement> processElementsByKeys(
        indexKey: StubIndexKey<K, T>,
        project: Project,
        scope: GlobalSearchScope,
        crossinline keyPredicate: (key: K) -> Boolean = { true },
        crossinline processor: (key: K, element: T) -> Boolean
    ): Boolean {
        if (DumbService.isDumb(project)) return true
        return StubIndex.getInstance().processAllKeys(indexKey, p@{ key ->
            ProgressManager.checkCanceled()
            if (keyPredicate(key)) {
                StubIndex.getInstance().processElements(indexKey, key, project, scope, T::class.java) { element ->
                    ProgressManager.checkCanceled()
                    processor(key, element)
                }
            }
            true
        }, scope)
    }

    /**
     * 遍历所有键，找到第一个满足 [predicate] 的元素，并调用 [processor]；若未找到，则使用 [getDefaultValue] 的返回值（可为 null）。
     *
     * 可在遍历每个键前调用 [resetDefaultValue] 重置外部缓存。
     *
     * Dumb 模式下直接返回 `true`。
     */
    inline fun <K : Any, reified T : PsiElement> processFirstElementByKeys(
        indexKey: StubIndexKey<K, T>,
        project: Project,
        scope: GlobalSearchScope,
        crossinline keyPredicate: (key: K) -> Boolean = { true },
        crossinline predicate: (element: T) -> Boolean = { true },
        crossinline getDefaultValue: () -> T? = { null },
        crossinline resetDefaultValue: () -> Unit = {},
        crossinline processor: (element: T) -> Boolean
    ): Boolean {
        if (DumbService.isDumb(project)) return true
        var value: T?
        return StubIndex.getInstance().processAllKeys(indexKey, p@{ key ->
            ProgressManager.checkCanceled()
            if (keyPredicate(key)) {
                value = null
                resetDefaultValue()
                StubIndex.getInstance().processElements(indexKey, key, project, scope, T::class.java) { element ->
                    ProgressManager.checkCanceled()
                    if (predicate(element)) {
                        value = element
                        return@processElements false
                    }
                    true
                }
                val finalValue = value ?: getDefaultValue()
                if (finalValue != null) {
                    val result = processor(finalValue)
                    if (!result) return@p false
                }
            }
            true
        }, scope)
    }
}

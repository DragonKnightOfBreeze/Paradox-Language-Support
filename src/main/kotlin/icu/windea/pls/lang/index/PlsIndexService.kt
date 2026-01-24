package icu.windea.pls.lang.index

import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.findFileBasedIndex
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo

object PlsIndexService {
    // region FileBasedIndex Methods

     fun <K, V> processFiles(
        indexId: ID<K, V>,
        keys: Collection<K>,
        project: Project,
        scope: GlobalSearchScope,
        processor: Processor<in VirtualFile>,
    ): Boolean {
        if (SearchScope.isEmptyScope(scope)) return true
        if (DumbService.isDumb(project)) return true
        ProgressManager.checkCanceled()

        val finalKeys = getKeysOrAllKeys(indexId, keys, scope)
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(indexId, finalKeys, scope, null, null, processor)
    }

    fun <T> processAllFileData(
        indexType: Class<out IndexInfoAwareFileBasedIndex<T>>,
        keys: Collection<String>,
        project: Project,
        scope: GlobalSearchScope,
        gameType: ParadoxGameType?,
        processor: (file: VirtualFile, fileData: Map<String, T>) -> Boolean
    ): Boolean {
        if (SearchScope.isEmptyScope(scope)) return true
        if (DumbService.isDumb(project)) return true
        ProgressManager.checkCanceled()

        val index = findFileBasedIndex(indexType)
        val indexId = index.name
        return processFilesWithKeys(indexId, keys, scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) // ensure file info is resolved here
            if (gameType != null && gameType != selectGameType(file)) return@p true // check game type at file level

            val fileData = index.getFileData(file, project)
            if (fileData.isEmpty()) return@p true
            processor(file, fileData)
        }
    }

    fun <T : ParadoxIndexInfo> processAllFileDataWithKey(
        indexInfoType: ParadoxIndexInfoType<T>,
        project: Project,
        scope: GlobalSearchScope,
        gameType: ParadoxGameType?,
        processor: (file: VirtualFile, infos: List<T>) -> Boolean
    ): Boolean {
        if (SearchScope.isEmptyScope(scope)) return true
        if (DumbService.isDumb(project)) return true
        ProgressManager.checkCanceled()

        val index = findFileBasedIndex(ParadoxMergedIndex::class.java)
        val indexId = index.name
        val key = indexInfoType.id.toString()
        val keys = setOf(key)
        return processFilesWithKeys(indexId, keys, scope) p@{ file ->
            ProgressManager.checkCanceled()
            ParadoxAnalysisManager.getFileInfo(file) // ensure file info is resolved here
            if (gameType != null && gameType != selectGameType(file)) return@p true // check game type at file level

            val infos = index.getFileDataWithKey(file, project, key).castOrNull<List<T>>()
            if (infos.isNullOrEmpty()) return@p true
            processor(file, infos)
        }
    }

     fun <K, V> processFilesWithKeys(
        indexId: ID<K, V>,
        keys: Collection<K>,
        scope: GlobalSearchScope,
        processor: Processor<in VirtualFile>,
    ): Boolean {
        val finalKeys = getKeysOrAllKeys(indexId, keys, scope)
        return FileBasedIndex.getInstance().processFilesContainingAnyKey(indexId, finalKeys, scope, null, null, processor)
    }

    private fun <K, V> getKeysOrAllKeys(indexId: ID<K, V>, keys: Collection<K>, scope: GlobalSearchScope): Collection<K> {
        if (keys.isNotEmpty()) return keys
        val project = scope.project ?: return emptySet()
        return FileBasedIndex.getInstance().getAllKeys(indexId, project)
    }

    // endregion

    // region StubIndex Methods

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
        if (SearchScope.isEmptyScope(scope)) return true
        if (DumbService.isDumb(project)) return true
        ProgressManager.checkCanceled()

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
        if (SearchScope.isEmptyScope(scope)) return true
        if (DumbService.isDumb(project)) return true
        ProgressManager.checkCanceled()

        // #241 try to avoid:
        // java.lang.IllegalStateException: Nesting processElements call under other stub index operation can lead to a deadlock.

        val keys = mutableSetOf<K>()
        StubIndex.getInstance().processAllKeys(indexKey, { key ->
            ProgressManager.checkCanceled()
            if (keyPredicate(key)) keys.add(key)
            true
        }, scope)
        return keys.process { key ->
            ProgressManager.checkCanceled()
            StubIndex.getInstance().processElements(indexKey, key, project, scope, T::class.java) { element ->
                ProgressManager.checkCanceled()
                processor(key, element)
            }
            true
        }
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
        if (SearchScope.isEmptyScope(scope)) return true
        if (DumbService.isDumb(project)) return true
        ProgressManager.checkCanceled()

        // #241 try to avoid:
        // java.lang.IllegalStateException: Nesting processElements call under other stub index operation can lead to a deadlock.

        val keys = mutableSetOf<K>()
        StubIndex.getInstance().processAllKeys(indexKey, { key ->
            ProgressManager.checkCanceled()
            if (keyPredicate(key)) keys.add(key)
            true
        }, scope)
        var value: T?
        return keys.process { key ->
            ProgressManager.checkCanceled()
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
                if (!result) return@process false
            }
            true
        }
    }

    /**
     * 根据指定的 [prefixMatcher] 和 [selector]，遍历所有变体。
     *
     * 用于优化代码补全的性能。
     */
    inline fun <reified T : PsiElement> processVariants(
        indexKey: StubIndexKey<String, T>,
        prefixMatcher: PrefixMatcher,
        selector: ParadoxSearchSelector<T>,
        processor: Processor<T>
    ): Boolean {
        // 保证返回结果的名字的唯一性
        return processFirstElementByKeys(
            indexKey,
            selector.project,
            selector.scope,
            keyPredicate = { key -> prefixMatcher.prefixMatches(key) },
            predicate = { element -> selector.selectOne(element) },
            getDefaultValue = { selector.getDefaultValue() },
            resetDefaultValue = { selector.resetDefaultValue() },
            processor = { processor.process(it) }
        )
    }

    // endregion
}

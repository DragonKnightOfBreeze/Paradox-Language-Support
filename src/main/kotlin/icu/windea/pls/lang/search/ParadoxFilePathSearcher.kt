package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.process
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.ParadoxIndexManager
import icu.windea.pls.lang.util.*

/**
 * 文件路径的查询器。
 */
class ParadoxFilePathSearcher : QueryExecutorBase<VirtualFile, ParadoxFilePathSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxFilePathSearch.SearchParameters, consumer: Processor<in VirtualFile>) {
        ProgressManager.checkCanceled()
        if(queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val filePath = queryParameters.filePath
        val configExpression = queryParameters.configExpression
        val project = queryParameters.project
        val gameType = queryParameters.selector.gameType
        val contextElement = queryParameters.selector.file?.toPsiFile(project)

        if (configExpression == null) {
            if (filePath == null) {
                val keys = FileBasedIndex.getInstance().getAllKeys(ParadoxIndexManager.FilePathName, project)
                FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxIndexManager.FilePathName, keys, scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    val fileInfo = ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
                    if (fileInfo == null) return@p true
                    if (gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    consumer.process(file)
                }
            } else {
                val keys = getFilePaths(filePath, queryParameters)
                FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxIndexManager.FilePathName, keys, scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    val fileInfo = ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
                    if (fileInfo == null) return@p true
                    if (gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    consumer.process(file)
                }
            }
        } else {
            val support = ParadoxPathReferenceExpressionSupport.get(configExpression) ?: return
            if (filePath == null) {
                val keys = mutableSetOf<String>()
                FileBasedIndex.getInstance().processAllKeys(ParadoxIndexManager.FilePathName, p@{ p ->
                    if (!support.matches(configExpression, contextElement, p)) return@p true
                    keys.add(p)
                }, scope, null)
                FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxIndexManager.FilePathName, keys, scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    val fileInfo = ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
                    if (fileInfo == null) return@p true
                    if (gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    consumer.process(file)
                }
            } else {
                val resolvedPaths = support.resolvePath(configExpression, filePath)
                if (resolvedPaths.isNotNullOrEmpty()) {
                    val keys = resolvedPaths
                    FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxIndexManager.FilePathName, keys, scope, null, null) p@{ file ->
                        ProgressManager.checkCanceled()
                        val fileInfo = ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
                        if (fileInfo == null) return@p true
                        if (gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                        consumer.process(file)
                    }
                    return
                }
                val resolvedFileNames = support.resolveFileName(configExpression, filePath)
                if (resolvedFileNames.isNotNullOrEmpty()) {
                    val resolvedFiles = sortedSetOf<VirtualFile>(compareBy { it.path })
                    FilenameIndex.processFilesByNames(resolvedFileNames, false, scope, null) p@{ file ->
                        ProgressManager.checkCanceled()
                        val fileInfo = ParadoxCoreManager.getFileInfo(file) //ensure file info is resolved here
                        if (fileInfo == null) return@p true
                        if (gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                        if (!support.matches(configExpression, contextElement, fileInfo.path.path)) return@p true
                        resolvedFiles.add(file)
                    }
                    resolvedFiles.process { consumer.process(it) }
                }
            }
        }
    }

    private fun getFilePaths(filePath: String, queryParameters: ParadoxFilePathSearch.SearchParameters): Set<String> {
        if (queryParameters.ignoreLocale) {
            return getFilePathsIgnoreLocale(filePath) ?: setOf(filePath)
        } else {
            return setOf(filePath)
        }
    }

    private fun getFilePathsIgnoreLocale(filePath: String): Set<String>? {
        if (!filePath.endsWith(".yml", true)) return null //仅限本地化文件
        val localeStrings = ParadoxLocaleManager.getLocaleConfigs().map { it.shortId }
        var index = 0
        var usedLocaleString: String? = null
        for (localeString in localeStrings) {
            val nextIndex = filePath.indexOf(localeString, index)
            if (nextIndex == -1) continue
            index = nextIndex + localeString.length
            if (usedLocaleString != localeString) {
                if (usedLocaleString != null) {
                    //类似将l_english.yml放到l_simp_chinese目录下的情况，此时直接不作处理
                    return null
                } else {
                    usedLocaleString = localeString
                }
            }
        }
        if (usedLocaleString == null) return null
        val result = mutableSetOf<String>()
        result.add(filePath)
        localeStrings.forEach { result.add(filePath.replace(usedLocaleString, it)) }
        return result
    }
}

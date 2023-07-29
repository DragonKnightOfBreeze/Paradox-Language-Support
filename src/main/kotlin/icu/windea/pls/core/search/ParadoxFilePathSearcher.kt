package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*

/**
 * 文件路径的查询器。
 */
class ParadoxFilePathSearcher : QueryExecutorBase<VirtualFile, ParadoxFilePathSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxFilePathSearch.SearchParameters, consumer: Processor<in VirtualFile>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if(SearchScope.isEmptyScope(scope)) return
        val filePath = queryParameters.filePath?.trimEnd('/')
        val configExpression = queryParameters.configExpression
        val project = queryParameters.project
        val gameType = queryParameters.selector.gameType
        val contextElement = queryParameters.selector.file?.toPsiFile(project)
        
        if(configExpression == null) {
            if(filePath == null) {
                val keys = FileBasedIndex.getInstance().getAllKeys(ParadoxFilePathIndexName, project)
                FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxFilePathIndexName, keys, scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
                    if(gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    consumer.process(file)
                }
            } else {
                val keys = getFilePaths(filePath, queryParameters)
                FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxFilePathIndexName, keys, scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
                    if(gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    consumer.process(file)
                }
            }
        } else {
            val support = ParadoxPathReferenceExpressionSupport.get(configExpression) ?: return
            if(filePath == null) {
                val keys = mutableSetOf<String>()
                FileBasedIndex.getInstance().processAllKeys(ParadoxFilePathIndexName, p@{ p ->
                    if(!support.matches(configExpression, contextElement, p)) return@p true
                    keys.add(p)
                }, scope, null)
                FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxFilePathIndexName, keys, scope, null, null) p@{ file ->
                    ProgressManager.checkCanceled()
                    ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
                    if(gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    consumer.process(file)
                }
            } else {
                val resolvedPath = support.resolvePath(configExpression, filePath)
                if(resolvedPath != null) {
                    val keys = setOf(resolvedPath)
                    FileBasedIndex.getInstance().processFilesContainingAnyKey(ParadoxFilePathIndexName, keys, scope, null, null) p@{ file ->
                        ProgressManager.checkCanceled()
                        ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
                        if(gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                        consumer.process(file)
                    }
                    return
                }
                val resolvedFileName = support.resolveFileName(configExpression, filePath)
                FilenameIndex.processFilesByName(resolvedFileName, true, scope) p@{ file ->
                    ProgressManager.checkCanceled()
                    val fileInfo = ParadoxCoreHandler.getFileInfo(file) ?: return@p true //ensure file info is resolved here
                    if(gameType != null && selectGameType(file) != gameType) return@p true //check game type at file level
                    val p = fileInfo.path.path
                    if(!support.matches(configExpression, contextElement, p)) return@p true
                    consumer.process(file)
                }
            }
        }
    }
    
    private fun getFilePaths(filePath: String, queryParameters: ParadoxFilePathSearch.SearchParameters): Set<String> {
        if(queryParameters.ignoreLocale) {
            return getFilePathsIgnoreLocale(filePath) ?: setOf(filePath)
        } else {
            return setOf(filePath)
        }
    }
    
    private fun getFilePathsIgnoreLocale(filePath: String): Set<String>? {
        if(!filePath.endsWith(".yml", true)) return null //仅限本地化文件
        val localeStrings = getCwtConfig().core.localisationLocalesNoDefaultNoPrefix.keys
        var index = 0
        var usedLocaleString: String? = null
        for(localeString in localeStrings) {
            val nextIndex = filePath.indexOf(localeString, index)
            if(nextIndex == -1) continue
            index = nextIndex + localeString.length
            if(usedLocaleString != localeString) {
                if(usedLocaleString != null) {
                    //类似将l_english.yml放到l_simp_chinese目录下的情况，此时直接不作处理
                    return null
                } else {
                    usedLocaleString = localeString
                }
            }
        }
        if(usedLocaleString == null) return null
        val result = mutableSetOf<String>()
        result.add(filePath)
        localeStrings.forEach { result.add(filePath.replace(usedLocaleString, it)) }
        return result
    }
}
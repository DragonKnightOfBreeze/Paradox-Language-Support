package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.PsiFile
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.expression.*

/**
 * 文件路径的查询器。
 */
class ParadoxFilePathSearcher : QueryExecutorBase<VirtualFile, ParadoxFilePathSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxFilePathSearch.SearchParameters, consumer: Processor<in VirtualFile>) {
        val filePath = queryParameters.filePath?.trimEnd('/')
        val configExpression = queryParameters.configExpression
        val ignoreCase = queryParameters.ignoreCase
        val project = queryParameters.project
        val scope = queryParameters.selector.scope
        val name = ParadoxFilePathIndex.NAME
        val contextElement = queryParameters.selector.file?.toPsiFile<PsiFile>(project)
        val pathReferenceExpressionSupport = if(configExpression != null) ParadoxPathReferenceExpressionSupport.get(configExpression) else null
        if(configExpression == null || pathReferenceExpressionSupport?.matchEntire(configExpression, contextElement) == true) {
            val keys = if(filePath != null) {
                getFilePaths(filePath, queryParameters)
            } else {
                FileBasedIndex.getInstance().getAllKeys(name, project)
            }
            FileBasedIndex.getInstance().processFilesContainingAnyKey(name, keys, scope, null, null) { file ->
                consumer.process(file)
            }
            return
        }
        if(pathReferenceExpressionSupport == null) return
        FileBasedIndex.getInstance().processAllKeys(name, p@{ path ->
            ProgressManager.checkCanceled()
            if(filePath != null && pathReferenceExpressionSupport.extract(configExpression, contextElement, path, ignoreCase) != filePath) return@p true
            if(!pathReferenceExpressionSupport.matches(configExpression, contextElement, path, ignoreCase)) return@p true
            val keys = setOf(path)
            FileBasedIndex.getInstance().processFilesContainingAnyKey(name, keys, scope, null, null) { file ->
                consumer.process(file)
            }
            true
        }, scope, null)
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
        val localeStrings = getCwtConfig().core.localisationLocales.keys.mapTo(mutableSetOf()) { it.removePrefix("l_") }
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
package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
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
        val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
        val name = ParadoxFilePathIndex.name
        if(configExpression == null) {
			val keys = if(filePath != null) setOf(filePath) else FileBasedIndex.getInstance().getAllKeys(name, project)
            FileBasedIndex.getInstance().processFilesContainingAnyKey(name, keys, scope, null, null) { file ->
                consumer.process(file)
            }
            return
        }
        val pathReferenceExpression = ParadoxPathReferenceExpression.get(configExpression) ?: return
		if(filePath == null) {
			FileBasedIndex.getInstance().processAllKeys(name, p@{ path ->
				if(!pathReferenceExpression.matches(configExpression, path, ignoreCase)) return@p true
				val keys = setOf(path)
				FileBasedIndex.getInstance().processFilesContainingAnyKey(name, keys, scope, null, null) { file ->
					consumer.process(file)
				}
				true
			}, scope, null)
		} else {
			if(!pathReferenceExpression.matches(configExpression, filePath, ignoreCase)) return
			val keys = setOf(filePath)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, keys, scope, null, null) { file ->
				consumer.process(file)
			}
		}
    }
}
package icu.windea.pls.core.search

import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.index.*

/**
 * 文件路径的查询器。
 */
class ParadoxFilePathSearcher: QueryExecutorBase<VirtualFile, ParadoxFilePathSearch.SearchParameters>() {
	override fun processQuery(queryParameters: ParadoxFilePathSearch.SearchParameters, consumer: Processor<in VirtualFile>) {
		val filePath = queryParameters.filePath?.trimEnd('/')
		val type = queryParameters.type
		val ignoreCase = queryParameters.ignoreCase
		val project = queryParameters.project
		val scope = GlobalSearchScopeUtil.toGlobalSearchScope(queryParameters.scope, project)
		val name = ParadoxFilePathIndex.name
		if(filePath != null) {
			val dataKeys = if(type == CwtPathExpressionType.Exact) {
				setOf(filePath)
			} else {
				var dataKey: String? = null
				FileBasedIndex.getInstance().processAllKeys(name, { path ->
					if(type.matches(filePath, path, ignoreCase)) {
						dataKey = path
						false
					} else {
						true
					}
				}, scope, null)
				if(dataKey == null) return
				setOf(dataKey)
			}
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				consumer.process(file)
			}
		} else {
			val allKeys = FileBasedIndex.getInstance().getAllKeys(name, project)
			if(allKeys.isEmpty()) return
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, allKeys, scope, null, null) { file ->
				consumer.process(file)
			}
		}
	}
}
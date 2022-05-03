package icu.windea.pls.core.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.containers.CollectionFactory
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*

object ParadoxFilePathIndex {
	val name = ID.create<String, Void>("paradox.file.path.index")
	
	fun findOne(filePath: String, scope: GlobalSearchScope, expressionType: CwtFilePathExpressionType, ignoreCase: Boolean): VirtualFile? {
		var result: VirtualFile? = null
		if(expressionType == CwtFilePathExpressionType.Exact) {
			val dataKeys = setOf(filePath)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				result = file
				false
			}
		} else {
			var dataKey: String? = null
			FileBasedIndex.getInstance().processAllKeys(name, { path ->
				if(expressionType.matches(filePath, path, ignoreCase)) {
					dataKey = path
					false
				} else {
					true
				}
			}, scope, null)
			if(dataKey == null) return null
			val dataKeys = setOf(dataKey)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				result = file
				false
			}
		}
		return result
	}
	
	fun findAll(filePath: String, scope: GlobalSearchScope, expressionType: CwtFilePathExpressionType, ignoreCase: Boolean, distinct: Boolean): Set<VirtualFile> {
		val result: MutableSet<VirtualFile> = CollectionFactory.createSmallMemoryFootprintLinkedSet() //优化性能
		if(expressionType == CwtFilePathExpressionType.Exact) {
			val dataKeys = setOf(filePath)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				result.add(file)
				true
			}
		} else {
			val dataKeys: MutableSet<String> = mutableSetOf()
			FileBasedIndex.getInstance().processAllKeys(name, { path ->
				if(expressionType.matches(filePath, path, ignoreCase)) {
					dataKeys.add(path)
				}
				true
			}, scope, null)
			if(dataKeys.isEmpty()) return emptySet()
			val keysToDistinct = if(distinct) mutableSetOf<String>() else null
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				if(keysToDistinct == null || file.fileInfo?.path?.path.let { it != null && keysToDistinct.add(if(ignoreCase) it.lowercase() else it) }) {
					result.add(file)
				}
				true
			}
		}
		return result
	}
	
	fun findAll(project: Project, scope: GlobalSearchScope, ignoreCase: Boolean, distinct: Boolean): Set<VirtualFile> {
		val result: MutableSet<VirtualFile> = CollectionFactory.createSmallMemoryFootprintLinkedSet() //优化性能
		val allKeys = FileBasedIndex.getInstance().getAllKeys(name, project)
		if(allKeys.isEmpty()) return emptySet()
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		FileBasedIndex.getInstance().processFilesContainingAnyKey(name, allKeys, scope, null, null) { file ->
			if(keysToDistinct == null || file.fileInfo?.path?.path.let { it != null && keysToDistinct.add(if(ignoreCase) it.lowercase() else it) }) {
				result.add(file)
			}
			true
		}
		return result
	}
}
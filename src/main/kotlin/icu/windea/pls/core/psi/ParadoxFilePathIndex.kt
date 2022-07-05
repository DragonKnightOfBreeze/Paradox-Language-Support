package icu.windea.pls.core.psi

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.util.selector.*

object ParadoxFilePathIndex {
	val name = ID.create<String, Void>("paradox.file.path.index")
	
	fun findOne(filePath: String, scope: GlobalSearchScope, expressionType: CwtFilePathExpressionType, ignoreCase: Boolean, selector: ChainedParadoxSelector<VirtualFile>): VirtualFile? {
		val usedFilePath = filePath.trimEnd('/')
		var result: VirtualFile? = null
		if(expressionType == CwtFilePathExpressionTypes.Exact) {
			val dataKeys = setOf(usedFilePath)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				if(selector.select(file)) {
					result = file
					false
				} else {
					true
				}
			}
		} else {
			var dataKey: String? = null
			FileBasedIndex.getInstance().processAllKeys(name, { path ->
				if(expressionType.matches(usedFilePath, path, ignoreCase)) {
					dataKey = path
					false
				} else {
					true
				}
			}, scope, null)
			if(dataKey == null) return null
			val dataKeys = setOf(dataKey)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				if(selector.select(file)) {
					result = file
					false
				} else {
					true
				}
			}
		}
		return result ?: selector.defaultValue
	}
	
	fun findAll(filePath: String, scope: GlobalSearchScope, expressionType: CwtFilePathExpressionType, ignoreCase: Boolean, distinct: Boolean, selector: ChainedParadoxSelector<VirtualFile>): Set<VirtualFile> {
		val usedFilePath = filePath.trimEnd('/')
		val result: MutableSet<VirtualFile> = MutableSet(selector.comparator())
		if(expressionType == CwtFilePathExpressionTypes.Exact) {
			val dataKeys = setOf(usedFilePath)
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
					result.add(file)
				true
			}
		} else {
			val dataKeys: MutableSet<String> = mutableSetOf()
			FileBasedIndex.getInstance().processAllKeys(name, { path ->
				if(expressionType.matches(usedFilePath, path, ignoreCase)) {
					dataKeys.add(path)
				}
				true
			}, scope, null)
			if(dataKeys.isEmpty()) return emptySet()
			val keysToDistinct = if(distinct) mutableSetOf<String>() else null
			FileBasedIndex.getInstance().processFilesContainingAnyKey(name, dataKeys, scope, null, null) { file ->
				if(keysToDistinct == null || file.fileInfo?.path?.path.let { it != null && keysToDistinct.add(if(ignoreCase) it.lowercase() else it) }) {
					if(selector.selectAll(file)) {
						result.add(file)
					}
				}
				true
			}
		}
		return result
	}
	
	fun findAll(project: Project, scope: GlobalSearchScope, ignoreCase: Boolean, distinct: Boolean, selector: ChainedParadoxSelector<VirtualFile>): Set<VirtualFile> {
		val result: MutableSet<VirtualFile> = MutableSet(selector.comparator())
		val allKeys = FileBasedIndex.getInstance().getAllKeys(name, project)
		if(allKeys.isEmpty()) return emptySet()
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		FileBasedIndex.getInstance().processFilesContainingAnyKey(name, allKeys, scope, null, null) { file ->
			if(keysToDistinct == null || file.fileInfo?.path?.path.let { it != null && keysToDistinct.add(if(ignoreCase) it.lowercase() else it) }) {
				if(selector.selectAll(file)) {
					result.add(file)
				}
			}
			true
		}
		return result
	}
}
package icu.windea.pls.core

import com.google.common.cache.LoadingCache
import icu.windea.pls.*

/**
 * 文件或目录相对于游戏或模组根路径的路径。
 *
 * 示例：
 * * `common/buildings/00_capital_buildings.txt`
 * * `localisation/simp_chinese/l_simp_chinese.yml`
 */
@Suppress("unused")
class ParadoxPath private constructor(
	val path: String
) : Iterable<String> {
	companion object Resolver {
		val EmptyPath = ParadoxPath("")
		
		private val cache: LoadingCache<String, ParadoxPath> = createCache { path -> ParadoxPath(path) }
		
		fun resolve(path: String) = cache[path]
		
		fun resolve(subPaths: List<String>) = cache[subPaths.joinToString("/")]
	}
	
	val subPaths = path.split("/")
	val length = subPaths.size
	val parentSubPaths = subPaths.dropLast(1)
	val parent = path.substringBeforeLast("/")
	val root = parentSubPaths.firstOrNull().orEmpty()
	val fileName = subPaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	
	fun isEmpty(): Boolean {
		return length == 0
	}
	
	override fun iterator(): Iterator<String> {
		return subPaths.iterator()
	}
	
	override fun toString(): String {
		return path
	}
}

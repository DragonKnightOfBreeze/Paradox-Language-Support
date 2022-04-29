package icu.windea.pls.core

import com.google.common.cache.LoadingCache
import icu.windea.pls.*

@Suppress("unused")
class ParadoxPath private constructor(
	val path: String
) : Iterable<String> {
	companion object Resolver {
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
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

package icu.windea.pls.config

import com.google.common.cache.*
import icu.windea.pls.core.util.*

/**
 * CWT规则的领。保留大小写。忽略括起的双引号。
 */
interface CwtConfigPath: Iterable<String> {
	val path: String
	val subPaths: List<String>
	val length: Int
	
	fun isEmpty(): Boolean = length == 0
	
	fun isNotEmpty(): Boolean = length != 0
	
	fun get(index: Int): String = this.subPaths.getOrNull(index).orEmpty()
	
	override fun iterator(): Iterator<String> = this.subPaths.iterator()
	
	override fun equals(other: Any?): Boolean
	
	override fun hashCode(): Int
	
	override fun toString(): String
	
	companion object Resolver {
		private val cache: LoadingCache<String, CwtConfigPath> = CacheBuilder.newBuilder().buildCache {
			if(it.isEmpty()) EmptyCwtConfigPath else CwtConfigPathImpl(it)
		}
		
		fun resolve(path: String): CwtConfigPath {
			return cache[path]
		}
		
		fun resolve(subPaths: List<String>): CwtConfigPath {
			return cache[subPaths.joinToString("/")]
		}
	}
}

class CwtConfigPathImpl(
	override val path: String
): CwtConfigPath {
	override val subPaths: List<String> = path.split('/')
	override val length = subPaths.size
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is CwtConfigPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

object EmptyCwtConfigPath : CwtConfigPath {
	override val path: String = ""
	override val subPaths: List<String> = emptyList()
	override val length: Int = 0
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is CwtConfigPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}
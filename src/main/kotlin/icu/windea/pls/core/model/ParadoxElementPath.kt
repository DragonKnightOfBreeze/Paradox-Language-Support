package icu.windea.pls.core.model

import com.google.common.cache.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 定义或定义属性相对于所属文件或定义的路径。保留大小写。
 *
 * 示例：
 * * （空字符串） - 对应所属文件或定义本身。
 * * `foo` - 对应所属文件或定义中名为"foo"的属性。
 * * `foo/bar` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性
 * * `foo/"bar"` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性（属性名在脚本中用引号括起）
 * * `#foo` - 对应值"foo"
 *
 * @property originalPath 保留括起的双引号的使用"/"分割的路径。
 * @property path 不保留括起的双引号的使用"/"分割的路径。
 * @property isParameterAware 路径中是否带有参数（即使无法解析或者语法错误）。
 */
interface ParadoxElementPath : Iterable<String> {
	val originalPath: String
	val originalSubPaths: List<String>
	val subPaths: List<String>
	val path: String
	val length: Int
	val isParameterAware: Boolean
	
	fun isEmpty(): Boolean {
		return length == 0
	}
	
	fun isNotEmpty(): Boolean {
		return length != 0
	}
	
	override fun iterator(): Iterator<String> {
		return originalSubPaths.iterator()
	}
	
	override fun equals(other: Any?): Boolean
	
	override fun hashCode(): Int
	
	override fun toString(): String
	
	companion object Resolver {
		private val cache: LoadingCache<String, ParadoxElementPath> = CacheBuilder.newBuilder().buildCache {
			if(it.isEmpty()) EmptyParadoxElementPath else ParadoxElementPathImpl(it)
		}
		
		fun resolve(path: String): ParadoxElementPath {
			return cache[path]
		}
		
		fun resolve(originalSubPaths: List<String>): ParadoxElementPath {
			return cache[originalSubPaths.joinToString("/")]
		}
	}
}

object EmptyParadoxElementPath : ParadoxElementPath {
	override val originalPath: String = ""
	override val originalSubPaths: List<String> = emptyList()
	override val subPaths: List<String> = emptyList()
	override val path: String = ""
	override val length: Int = 0
	override val isParameterAware: Boolean = false
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxElementPath && originalPath == other.originalPath
	}
	
	override fun hashCode(): Int {
		return originalPath.hashCode()
	}
	
	override fun toString(): String {
		return originalPath
	}
}

class ParadoxElementPathImpl(
	override val originalPath: String
) : ParadoxElementPath {
	override val originalSubPaths: List<String> = originalPath.split('/')
	override val subPaths = originalSubPaths.map { it.unquote() }
	override val path = subPaths.joinToString("/")
	override val length = originalSubPaths.size
	override val isParameterAware = length != 0 && originalSubPaths.any { it.isParameterAwareExpression() }
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxElementPath && originalPath == other.originalPath
	}
	
	override fun hashCode(): Int {
		return originalPath.hashCode()
	}
	
	override fun toString(): String {
		return originalPath
	}
}
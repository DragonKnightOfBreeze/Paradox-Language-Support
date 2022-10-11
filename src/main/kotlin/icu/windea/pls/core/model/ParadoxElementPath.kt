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
 */
class ParadoxElementPath private constructor(
	val originalPath: String
) : Iterable<String> {
	companion object Resolver {
		val EmptyPath = ParadoxElementPath("")
		
		private val cache: LoadingCache<String, ParadoxElementPath> = CacheBuilder.newBuilder().buildCache { ParadoxElementPath(it) }
		
		fun resolve(path: String) = cache[path]
		
		fun resolve(originalSubPaths: List<String>) = cache[originalSubPaths.joinToString("/")]
	}
	
	val originalSubPaths: List<String> = originalPath.split('/')
	
	val subPaths = originalSubPaths.map { it.unquote() }
	
	val path = subPaths.joinToString("/")
	
	val length = originalSubPaths.size
	
	/**
	 * 路径中是否带有参数（即使无法解析或者语法错误）。
	 */
	val isParameterAware = length != 0 && originalSubPaths.any { it.isParameterAwareExpression() }
	
	fun isEmpty(): Boolean {
		return length == 0
	}
	
	fun isNotEmpty(): Boolean {
		return length != 0
	}
	
	override fun iterator(): Iterator<String> {
		return originalSubPaths.iterator()
	}
	
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
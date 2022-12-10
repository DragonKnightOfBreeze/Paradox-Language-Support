package icu.windea.pls.core.model

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 定义或定义属性相对于所属文件或定义的路径。保留大小写。
 *
 * 示例：
 * * （空字符串） - 对应所属文件或定义本身。
 * * `foo` - 对应所属文件或定义中名为"foo"的属性。
 * * `foo/bar` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性
 * * `foo/"bar"` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性（属性名在脚本中用引号括起）
 * * `foo/-` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，任意的值
 *
 * @property path 使用"/"分割的路径（保留括起的双引号）。
 * @property isParameterAware 路径中是否带有参数（即使无法解析或者语法错误）。
 */
interface ParadoxElementPath : Iterable<Tuple3<String, Boolean, Boolean>> {
	val path: String
	val subPaths: List<String>
	val subPathInfos: List<Tuple3<String, Boolean, Boolean>> //(unquotedSubPath, quoted, isKey)
	val length: Int
	val isParameterAware: Boolean
	
	fun isEmpty(): Boolean {
		return length == 0
	}
	
	fun isNotEmpty(): Boolean {
		return length != 0
	}
	
	override fun iterator(): Iterator<Tuple3<String, Boolean, Boolean>> {
		return this.subPathInfos.iterator()
	}
	
	/**
	 * 得到另一个子路径列表相对于当前元素路径的子路径列表中的第一个。例如，`"/foo/bar/x" relativeTo "/foo" -> "bar"`。
	 * 如果两者完全匹配，则返回空字符串。
	 * * @param ignoreCase 是否忽略大小写。默认为`true`。
	 * * @param useAnyWildcard 对于另一个子路径列表，是否使用`"any"`字符串作为子路径通配符，表示匹配任意子路径。默认为`true`。
	 */
	fun relativeTo(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true) : String? {
		if(this.length > other.size) return null
		for((index, subPathInfo) in this.subPathInfos.withIndex()) {
			val thisPath = subPathInfo.first
			val otherPath = other[index]
			if(useAnyWildcard && otherPath == "any") continue
			if(!thisPath.equals(otherPath, ignoreCase)) return null
		}
		if(this.length == other.size) return ""
		return other[this.length]
	}
	
	/**
	 * 判断当前元素路径是否匹配另一个子路径列表。使用"/"作为路径分隔符。
	 * @param ignoreCase 是否忽略大小写。默认为`true`。
	 * @param useAnyWildcard 对于另一个子路径列表，是否使用`"any"`字符串作为子路径通配符，表示匹配任意子路径。默认为`true`。
	 * @param useParentPath 是否需要仅匹配当前元素路径的父路径。默认为`false`。
	 */
	fun matchEntire(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true, useParentPath: Boolean = false): Boolean {
		val thisLength = if(useParentPath) length - 1 else length
		val otherLength = other.size
		if(thisLength < 0 || thisLength != otherLength) return false //路径过短或路径长度不一致
		for((index, otherPath) in other.withIndex()) {
			if(useAnyWildcard && otherPath == "any") continue
			val thisPath = subPathInfos[index].first
			if(!thisPath.equals(otherPath, ignoreCase)) return false
		}
		return true
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
	override val path: String = ""
	override val subPaths: List<String> = emptyList()
	override val subPathInfos: List<Tuple3<String, Boolean, Boolean>> = emptyList()
	override val length: Int = 0
	override val isParameterAware: Boolean = false
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxElementPath && this.path == other.path
	}
	
	override fun hashCode(): Int {
		return this.path.hashCode()
	}
	
	override fun toString(): String {
		return this.path
	}
}

class ParadoxElementPathImpl(
	override val path: String
) : ParadoxElementPath {
	override val subPaths: List<String> = path.split('/')
	override val length = subPaths.size
	override val subPathInfos: List<Tuple3<String, Boolean, Boolean>> = subPaths.map { 
		tupleOf(it.unquote(), it.isLeftQuoted(), it != "-")
	}
	override val isParameterAware = length != 0 && this.subPaths.any { it.isParameterAwareExpression() }
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxElementPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

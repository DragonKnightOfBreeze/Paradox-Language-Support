package icu.windea.pls.model.path

import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

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
 * @property isParameterized 路径中是否带有参数（即使无法解析或者存在语法错误）。
 */
interface ParadoxElementPath : Iterable<ParadoxElementPath.Info> {
    val path: String
    val rawSubPaths: List<String>
    val subPaths: List<Info>
    val length: Int
    
    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    
    override fun iterator(): Iterator<Info> = this.subPaths.iterator()
    
    companion object Resolver {
        val Empty: ParadoxElementPath = EmptyParadoxElementPath
        
        fun resolve(path: String): ParadoxElementPath = doResolve(path)
        
        fun resolve(rawSubPaths: List<String>): ParadoxElementPath = doResolve(rawSubPaths)
    }
    
    data class Info(
        val rawSubPath: String,
        val subPath: String,
        val isQuoted: Boolean,
        val isKey: Boolean,
    ) {
        companion object Resolver {
            fun resolve(path: String): Info = doResolveInfo(path)
        }
    }
}

fun ParadoxElementPath.relativeTo(other: ParadoxElementPath): ParadoxElementPath? {
    if(this == other) return EmptyParadoxElementPath
    if(this.isEmpty()) return other
    val path = other.path.removePrefixOrNull(this.path + "/") ?: return null
    return ParadoxElementPath.resolve(path)
}

/**
 * 得到另一个子路径列表相对于当前元素路径的子路径列表中的第一个。例如，`"/foo/bar/x" relativeTo "/foo" -> "bar"`。
 * 如果两者完全匹配，则返回空字符串。
 * @param ignoreCase 是否忽略大小写。默认为`true`。
 * @param useAnyWildcard 对于另一个子路径列表，是否使用`"any"`字符串作为子路径通配符，表示匹配任意子路径。默认为`true`。
 */
fun ParadoxElementPath.relativeTo(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true): String? {
    if(this.length > other.size) return null
    for((index, subPathInfo) in this.subPaths.withIndex()) {
        val thisPath = subPathInfo.subPath
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
fun ParadoxElementPath.matchEntire(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true, useParentPath: Boolean = false): Boolean {
    val thisLength = if(useParentPath) length - 1 else length
    val otherLength = other.size
    if(thisLength < 0 || thisLength != otherLength) return false //路径过短或路径长度不一致
    for((index, otherPath) in other.withIndex()) {
        if(useAnyWildcard && otherPath == "any") continue
        val thisPath = subPaths[index].subPath
        if(!thisPath.equals(otherPath, ignoreCase)) return false
    }
    return true
}

//Implementations (interned)

private fun doResolve(path: String): ParadoxElementPath {
    if(path.isEmpty()) return EmptyParadoxElementPath
    return ParadoxElementPathImplA(path)
}

private fun doResolve(rawSubPaths: List<String>): ParadoxElementPath {
    if(rawSubPaths.isEmpty()) return EmptyParadoxElementPath
    return ParadoxElementPathImplB(rawSubPaths)
}

private fun doResolveInfo(path: String): ParadoxElementPath.Info {
    return ParadoxElementPath.Info(path.intern(), path.unquote().intern(), path.isLeftQuoted(), path != "-")
}

private class ParadoxElementPathImplA(
    path: String
) : ParadoxElementPath {
    override val path: String = path.intern()
    override val rawSubPaths: List<String> = buildList {
        val builder = StringBuilder()
        var escape = false
        path.forEachFast { c ->
            when {
                c == '\\' -> {
                    escape = true
                }
                c == '/' && !escape -> {
                    add(builder.toString().intern())
                    builder.clear()
                }
                else -> {
                    if(escape) escape = false
                    builder.append(c)
                }
            }
        }
        if(builder.isNotEmpty()) {
            add(builder.toString().intern())
        }
    }
    override val subPaths: List<ParadoxElementPath.Info> = rawSubPaths.mapFast { ParadoxElementPath.Info.resolve(it) }
    override val length: Int = subPaths.size
    
    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxElementPathImplB(
    rawSubPaths: List<String>
) : ParadoxElementPath {
    override val path: String = buildString {
        var isFirst = true
        rawSubPaths.forEachFast { p ->
            if(isFirst) isFirst = false else append('/')
            p.forEachFast { c ->
                if(c == '/') append('\\')
                append(c)
            }
        }
    }.intern()
    override val rawSubPaths: List<String> = rawSubPaths.mapFast { it.intern() }
    override val subPaths: List<ParadoxElementPath.Info> = rawSubPaths.mapFast { ParadoxElementPath.Info.resolve(it) }
    override val length: Int = subPaths.size
    
    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private object EmptyParadoxElementPath: ParadoxElementPath {
    override val path: String = ""
    override val rawSubPaths: List<String> = emptyList()
    override val subPaths: List<ParadoxElementPath.Info> = emptyList()
    override val length: Int = 0
    
    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}
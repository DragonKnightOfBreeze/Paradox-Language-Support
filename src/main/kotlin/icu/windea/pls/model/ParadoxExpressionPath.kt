package icu.windea.pls.model

import icu.windea.pls.core.*

/**
 * 表达式路径。保留大小写。
 *
 * 可以用来表示：
 * * 定义成员相对于所属定义的路径。
 * * 定义相对于所在文件的路径。
 *
 * 示例：
 * * （空字符串） - 对应所属文件或定义本身。
 * * `foo` - 对应所属文件或定义中名为"foo"的属性。
 * * `foo/bar` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性
 * * `foo/"bar"` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性（属性名在脚本中用引号括起）
 * * `foo/-` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，任意的值
 *
 * @property path 使用"/"分隔的路径（预先移除括起的双引号）。
 * @property originalPath 使用"/"分隔的路径（保留括起的双引号）。
 */
interface ParadoxExpressionPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val originalPath: String
    val originalSubPaths: List<String>
    val length: Int
    
    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    
    override fun iterator(): Iterator<String> = this.subPaths.iterator()
    
    companion object Resolver {
        val Empty: ParadoxExpressionPath = EmptyParadoxExpressionPath
        
        fun resolve(originalPath: String): ParadoxExpressionPath = doResolve(originalPath)
        
        fun resolve(originalSubPaths: List<String>): ParadoxExpressionPath = doResolve(originalSubPaths)
    }
}

fun ParadoxExpressionPath.relativeTo(other: ParadoxExpressionPath): ParadoxExpressionPath? {
    if(this == other) return EmptyParadoxExpressionPath
    if(this.isEmpty()) return other
    val path = other.path.removePrefixOrNull(this.path + "/") ?: return null
    return ParadoxExpressionPath.resolve(path)
}

/**
 * 得到另一个子路径列表相对于当前表达式路径的子路径列表中的第一个。例如，`"/foo/bar/x" relativeTo "/foo" -> "bar"`。
 * 如果两者完全匹配，则返回空字符串。
 * @param ignoreCase 是否忽略大小写。默认为`true`。
 * @param useAnyWildcard 对于另一个子路径列表，是否使用`"any"`字符串作为子路径通配符，表示匹配任意子路径。默认为`true`。
 */
fun ParadoxExpressionPath.relativeTo(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true): String? {
    if(this.length > other.size) return null
    for((index, subPath) in this.subPaths.withIndex()) {
        val otherPath = other[index]
        if(useAnyWildcard && otherPath == "any") continue
        if(!subPath.equals(otherPath, ignoreCase)) return null
    }
    if(this.length == other.size) return ""
    return other[this.length]
}

/**
 * 判断当前表达式路径是否匹配另一个子路径列表。使用"/"作为路径分隔符。
 * @param ignoreCase 是否忽略大小写。默认为`true`。
 * @param useAnyWildcard 对于另一个子路径列表，是否使用`"any"`字符串作为子路径通配符，表示匹配任意子路径。默认为`true`。
 * @param useParentPath 是否需要仅匹配当前表达式路径的父路径。默认为`false`。
 */
fun ParadoxExpressionPath.matchEntire(other: List<String>, ignoreCase: Boolean = true, useAnyWildcard: Boolean = true, useParentPath: Boolean = false): Boolean {
    val thisLength = if(useParentPath) length - 1 else length
    val otherLength = other.size
    if(thisLength < 0 || thisLength != otherLength) return false //路径过短或路径长度不一致
    for((index, otherPath) in other.withIndex()) {
        if(useAnyWildcard && otherPath == "any") continue
        val thisPath = subPaths[index]
        if(!thisPath.equals(otherPath, ignoreCase)) return false
    }
    return true
}

//Implementations (interned)

private fun doResolve(originalPath: String): ParadoxExpressionPath {
    if(originalPath.isEmpty()) return EmptyParadoxExpressionPath
    return ParadoxExpressionPathImplA(originalPath)
}

private fun doResolve(originalSubPaths: List<String>): ParadoxExpressionPath {
    if(originalSubPaths.isEmpty()) return EmptyParadoxExpressionPath
    return ParadoxExpressionPathImplB(originalSubPaths)
}

//to optimize memory, it's better to use cache, or make property 'path' and 'originalPath' computed 

//12 + 4 * 4 = 28 -> 32
private class ParadoxExpressionPathImplA(
    path: String
) : ParadoxExpressionPath {
    override val originalPath: String = path.intern()
    override val originalSubPaths: List<String> = path2SubPaths(path)
    override val subPaths: List<String> = originalSubPaths.map { it.unquote().intern() }
    override val path: String = subPaths2Path(subPaths)
    override val length: Int get() = subPaths.size
    
    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

//12 + 4 * 4 = 28 -> 32
private class ParadoxExpressionPathImplB(
    originalSubPaths: List<String>
) : ParadoxExpressionPath {
    override val originalSubPaths: List<String> = originalSubPaths.map { it.intern() }
    override val originalPath: String = subPaths2Path(originalSubPaths)
    override val subPaths: List<String> = originalSubPaths.map { it.unquote().intern() }
    override val path: String = subPaths2Path(subPaths)
    override val length: Int get() = originalSubPaths.size
    
    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private object EmptyParadoxExpressionPath : ParadoxExpressionPath {
    override val path: String = ""
    override val subPaths: List<String> = emptyList()
    override val originalPath: String = ""
    override val originalSubPaths: List<String> = emptyList()
    override val length: Int = 0
    
    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private fun path2SubPaths(path: String): List<String> {
    return buildList {
        val builder = StringBuilder()
        var escape = false
        path.forEach { c ->
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
}

private fun subPaths2Path(originalSubPaths: List<String>): String {
    val builder = StringBuilder()
    var isFirst = true
    originalSubPaths.forEach { p ->
        if(isFirst) isFirst = false else builder.append('/')
        p.forEach { c ->
            if(c == '/') builder.append('\\')
            builder.append(c)
        }
    }
    return builder.toString().intern()
}

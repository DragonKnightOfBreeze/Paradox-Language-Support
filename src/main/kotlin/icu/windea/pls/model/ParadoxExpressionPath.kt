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
    if (this == other) return EmptyParadoxExpressionPath
    if (this.isEmpty()) return other
    val path = other.path.removePrefixOrNull(this.path + "/") ?: return null
    return ParadoxExpressionPath.resolve(path)
}

//Implementations (not interned)

private fun doResolve(originalPath: String): ParadoxExpressionPath {
    if (originalPath.isEmpty()) return EmptyParadoxExpressionPath
    return ParadoxExpressionPathImpl1(originalPath)
}

private fun doResolve(originalSubPaths: List<String>): ParadoxExpressionPath {
    if (originalSubPaths.isEmpty()) return EmptyParadoxExpressionPath
    val mayBeQuoted = originalSubPaths.any { it.isQuoted('"') }
    if (mayBeQuoted) return ParadoxExpressionPathImpl2(originalSubPaths)
    return ParadoxExpressionPathImpl3(originalSubPaths)
}

private abstract class ParadoxExpressionPathImpl : ParadoxExpressionPath {
    override val length: Int get() = originalSubPaths.size

    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && originalPath == other.originalPath
    override fun hashCode() = originalPath.hashCode()
    override fun toString() = originalPath

    protected fun path2SubPaths(path: String): List<String> {
        return buildList {
            val builder = StringBuilder()
            var escape = false
            path.forEach { c ->
                when {
                    c == '\\' -> {
                        escape = true
                    }
                    c == '/' && !escape -> {
                        if (builder.isNotEmpty()) add(builder.toString())
                        builder.clear()
                    }
                    else -> {
                        if (escape) escape = false
                        builder.append(c)
                    }
                }
            }
            if (builder.isNotEmpty()) add(builder.toString())
        }
    }

    protected fun subPaths2Path(subPaths: List<String>): String {
        val builder = StringBuilder()
        var isFirst = true
        subPaths.forEach { p ->
            if (isFirst) isFirst = false else builder.append('/')
            p.forEach { c ->
                if (c == '/') builder.append('\\')
                builder.append(c)
            }
        }
        return builder.toString()
    }
}

private class ParadoxExpressionPathImpl1(originalPath: String) : ParadoxExpressionPathImpl() {
    override val subPaths: List<String> by lazy { originalSubPaths.map { it.unquote() } }
    override val path: String by lazy { subPaths2Path(subPaths) }
    override val originalPath: String = originalPath
    override val originalSubPaths: List<String> by lazy { path2SubPaths(originalPath) }
}

private class ParadoxExpressionPathImpl2(originalSubPaths: List<String>) : ParadoxExpressionPathImpl() {
    override val subPaths: List<String> by lazy { originalSubPaths.map { it.unquote() } }
    override val path: String by lazy { subPaths2Path(subPaths) }
    override val originalPath: String = subPaths2Path(originalSubPaths)
    override val originalSubPaths: List<String> = originalSubPaths
}

private class ParadoxExpressionPathImpl3(originalSubPaths: List<String>) : ParadoxExpressionPathImpl() {
    override val path: String get() = originalPath
    override val subPaths: List<String> get() = originalSubPaths
    override val originalPath: String = subPaths2Path(originalSubPaths)
    override val originalSubPaths: List<String> = originalSubPaths
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

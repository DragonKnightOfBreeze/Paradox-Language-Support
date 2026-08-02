@file:Optimized

package icu.windea.pls.model.paths

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.ImmutableList
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.core.splitFast
import icu.windea.pls.core.util.values.LazyValue

/**
 * 脚本成员在脚本文件中的路径。
 *
 * 说明：
 * - 相对于脚本文件、定义或其他脚本成员。
 * - 如果对应深度的脚本成员是属性，则对应的子路径需要匹配其键。如果是单独的值，则需要是 `-`。
 * - 使用 "/" 分隔子路径。如果子路径中存在 "/"，会先用反引号转义。
 * - 保留大小写。
 * - 去除括起的双引号。
 *
 * 示例：
 * - （空字符串） - 对应所属脚本文件、定义或脚本成员本身。
 * - `foo` - 对应所属脚本文件、定义或脚本成员中，名为 `foo` 的属性。
 * - `foo/bar` - 对应所属脚本文件、定义或脚本成员中，名为 `foo` 的属性的值（块/子句）中，名为 `bar` 的属性。
 * - `foo/-` - 对应所属脚本文件、定义或脚本成员，名为 `foo` 的属性的值（块/子句）中，任意的值。
 */
interface ParadoxMemberPath : Iterable<String> {
    val path: String
    val subPaths: List<String> // 子路径中不用保留括起的双引号
    val length: Int

    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun get(index: Int): String

    fun normalize(): ParadoxMemberPath
    fun resolve(other: ParadoxMemberPath): ParadoxMemberPath?
    fun relativize(other: ParadoxMemberPath, wildcard: String? = null): ParadoxMemberPath?

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        @JvmStatic
        fun resolveEmpty(): ParadoxMemberPath = ParadoxMemberPathResolver.resolveEmpty()

        @JvmStatic
        fun resolve(input: String): ParadoxMemberPath = ParadoxMemberPathResolver.resolve(input)

        @JvmStatic
        fun resolve(input: List<String>): ParadoxMemberPath = ParadoxMemberPathResolver.resolve(input)
    }
}

// region Implementations

private object ParadoxMemberPathResolver {
    fun resolveEmpty(): ParadoxMemberPath = EmptyParadoxMemberPath

    fun resolve(input: String): ParadoxMemberPath {
        if (input.isEmpty()) return EmptyParadoxMemberPath
        return ParadoxMemberPathImplFromPath(input)
    }

    fun resolve(input: List<String>): ParadoxMemberPath {
        if (input.isEmpty()) return EmptyParadoxMemberPath
        return ParadoxMemberPathImplFromSubPaths(input)
    }
}

private sealed class ParadoxMemberPathBase : ParadoxMemberPath {
    override val length: Int get() = subPaths.size

    override fun isEmpty(): Boolean = length == 0

    override fun isNotEmpty(): Boolean = length != 0

    override fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()

    override fun normalize(): ParadoxMemberPath {
        if (this is NormalizedParadoxMemberPath || this is EmptyParadoxMemberPath) return this
        if (this.isEmpty()) return EmptyParadoxMemberPath
        return NormalizedParadoxMemberPath(this)
    }

    override fun resolve(other: ParadoxMemberPath): ParadoxMemberPath {
        if (other.isEmpty()) return this
        val subPaths = this.subPaths + other.subPaths
        return ParadoxMemberPath.resolve(subPaths)
    }

    override fun relativize(other: ParadoxMemberPath, wildcard: String?): ParadoxMemberPath? {
        // 3.0.1 optimize: do not check equality first
        if (this.isEmpty()) return other
        val subPaths = other.subPaths.removePrefixOrNull(this.subPaths, wildcard) ?: return null
        return ParadoxMemberPath.resolve(subPaths)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxMemberPath && subPaths == other.subPaths // 3.0.1 optimize: depends on `subPath` to avoid computing
    override fun hashCode() = subPaths.hashCode() // 3.0.1 optimize: depends on `subPath` to avoid computing
    override fun toString() = path
}

// 3.0.1 note: path object will not be self interned atm
private val pathInterner = Interner.newWeakInterner<String>()
private val subPathInterner = Interner.newWeakInterner<String>()

private fun String.internPath() = pathInterner.intern(this)
private fun String.internSubPath() = subPathInterner.intern(this)

private fun computePath(subPaths: List<String>): String {
    if(subPaths.anyFast { it.contains('/') }) {
        if (subPaths.size == 1) return subPaths[0].replace("/", "\\/")
        return subPaths.joinToString("/") { it.replace("/", "\\/") }
    }
    if (subPaths.size == 1) return subPaths[0]
    return subPaths.joinToString("/")
}

private fun computeSubPaths(path: String): List<String> {
    if (path.contains('\\')) {
        return path.replace("\\/", "\u0000").splitFast('/').mapFast { it.replace('\u0000', '/') }
    }
    return path.splitFast('/')
}

private fun computeNormalizedPath(subPaths: List<String>): String {
    if (subPaths.size == 1) return subPaths[0].replace("/", "\\/").internPath()
    return subPaths.joinToString("/") { it.replace("/", "\\/") }.internPath()
}

private fun computeNormalizedSubPaths(subPaths: List<String>): List<String> {
    return ImmutableList(subPaths.size) { subPaths[it].internSubPath() }
}

private class ParadoxMemberPathImplFromPath(input: String) : ParadoxMemberPathBase() {
    override val path: String = input
    override val subPaths: List<String> = computeSubPaths(path)
}

private class ParadoxMemberPathImplFromSubPaths(input: List<String>) : ParadoxMemberPathBase() {
    // 3.0.1 optimize: lazy compute `path` since it's rarely used in production code
    override val path: String // region by lazy { computePath(subPaths) }
        get() = LazyValue.of({ _path }, { _path = it }) { computePath(subPaths) }
    @Volatile private var _path: String? = null // endregion
    override val subPaths: List<String> = input
}

private class NormalizedParadoxMemberPath(input: ParadoxMemberPath) : ParadoxMemberPathBase() {
    // 3.0.1 optimize: lazy compute `path` since it's rarely used in production code
    override val path: String // region by lazy { computeNormalizedPath(subPaths) }
        get() = LazyValue.of({ _path }, { _path = it }) { computeNormalizedPath(subPaths) }
    @Volatile private var _path: String? = null // endregion
    override val subPaths: List<String> = computeNormalizedSubPaths(input.subPaths)
}

private object EmptyParadoxMemberPath : ParadoxMemberPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

// endregion

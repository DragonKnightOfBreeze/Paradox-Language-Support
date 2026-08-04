@file:Optimized

package icu.windea.pls.model.paths

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.buildImmutableList
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.core.joinToStringFast
import icu.windea.pls.core.splitFast
import icu.windea.pls.core.util.values.LazyValue

/**
 * 规则在规则文件中的路径。
 *
 * 说明：
 * - 相对于规则文件或其他成员规则。
 * - 如果对应深度的成员规则是属性规则，则对应的子路径需要匹配其键。如果是单独的值规则，则需要是 `-`。
 * - 使用 "/" 分隔子路径。如果子路径中存在 "/"，会先用反引号转义。
 * - 保留大小写。
 * - 去除括起的双引号。
 *
 * 示例：
 * - （空字符串） - 对应所属规则文件或成员规则本身。
 * - `foo` - 对应所属规则文件或成员规则中，名为 `foo` 的属性。
 * - `foo/bar` - 对应所属规则文件或成员规则中，名为 `foo` 的属性的值（块/子句）中，名为 `bar` 的属性。
 * - `foo/-` - 对应所属规则文件或成员规则中，名为 `foo` 的属性的值（块/子句）中，任意的值。
 */
interface CwtConfigPath {
    val path: String
    val subPaths: List<String> // 子路径中不用保留括起的双引号
    val length: Int

    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun get(index: Int): String

    fun normalize(): CwtConfigPath
    fun resolve(other: CwtConfigPath): CwtConfigPath?
    fun relativize(other: CwtConfigPath, wildcard: String? = null): CwtConfigPath?

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        @JvmStatic
        fun resolveEmpty(): CwtConfigPath = CwtConfigPathResolver.resolveEmpty()

        @JvmStatic
        fun resolve(input: String): CwtConfigPath = CwtConfigPathResolver.resolve(input)

        @JvmStatic
        fun resolve(input: List<String>): CwtConfigPath = CwtConfigPathResolver.resolve(input)
    }
}

// region Implementations

private object CwtConfigPathResolver {
    fun resolveEmpty(): CwtConfigPath = EmptyCwtConfigPath

    fun resolve(input: String): CwtConfigPath {
        if (input.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImplFromPath(input)
    }

    fun resolve(input: List<String>): CwtConfigPath {
        if (input.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImplFromSubPaths(input)
    }
}

private sealed class CwtConfigPathBase : CwtConfigPath {
    override val length: Int get() = subPaths.size

    override fun isEmpty(): Boolean = length == 0

    override fun isNotEmpty(): Boolean = length != 0

    override fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()

    override fun normalize(): CwtConfigPath {
        if (this is NormalizedCwtConfigPath || this is EmptyCwtConfigPath) return this
        if (this.isEmpty()) return EmptyCwtConfigPath
        return NormalizedCwtConfigPath(this)
    }

    override fun resolve(other: CwtConfigPath): CwtConfigPath {
        if (other.isEmpty()) return this
        val subPaths = this.subPaths + other.subPaths
        return CwtConfigPath.resolve(subPaths)
    }

    override fun relativize(other: CwtConfigPath, wildcard: String?): CwtConfigPath? {
        // 3.0.1 optimize: do not check equality first
        if (this.isEmpty()) return other
        val subPaths = other.subPaths.removePrefixOrNull(this.subPaths, wildcard) ?: return null
        return CwtConfigPath.resolve(subPaths)
    }

    override fun equals(other: Any?) = this === other || other is CwtConfigPath && subPaths == other.subPaths // 3.0.1 optimize: depends on `subPath` to avoid computing
    override fun hashCode() = subPaths.hashCode() // 3.0.1 optimize: depends on `subPath` to avoid computing
    override fun toString() = path
}

// 3.0.1 note: path object will not be self interned atm
private val pathInterner = Interner.newWeakInterner<String>()
private val subPathInterner = Interner.newWeakInterner<String>()

private fun String.internPath() = pathInterner.intern(this)
private fun String.internSubPath() = subPathInterner.intern(this)

private fun computePath(subPaths: List<String>): String {
    if (subPaths.anyFast { it.contains('/') }) {
        return subPaths.joinToStringFast("/") { it.replace("/", "\\/") }
    }
    return subPaths.joinToStringFast("/")
}

private fun computeSubPaths(path: String): List<String> {
    if (path.contains('\\')) {
        return path.replace("\\/", "\u0000").splitFast('/').mapFast { it.replace('\u0000', '/') }
    }
    return path.splitFast('/')
}

private fun computeNormalizedPath(subPaths: List<String>): String {
    return subPaths.joinToStringFast("/") { it.replace("/", "\\/") }.internPath()
}

private fun computeNormalizedSubPaths(subPaths: List<String>): List<String> {
    return buildImmutableList(subPaths.size) { subPaths[it].internSubPath() }
}

private class CwtConfigPathImplFromPath(input: String) : CwtConfigPathBase() {
    override val path: String = input
    override val subPaths: List<String> = computeSubPaths(path)
}

private class CwtConfigPathImplFromSubPaths(input: List<String>) : CwtConfigPathBase() {
    // 3.0.1 optimize: lazy compute `path` since it's rarely used in production code
    override val path: String // region by lazy { computePath(subPaths) }
        get() = LazyValue.of({ _path }, { _path = it }) { computePath(subPaths) }
    @Volatile private var _path: String? = null // endregion
    override val subPaths: List<String> = input
}

private class NormalizedCwtConfigPath(input: CwtConfigPath) : CwtConfigPathBase() {
    // 3.0.1 optimize: lazy compute `path` since it's rarely used in production code
    override val path: String // region by lazy { computeNormalizedPath(subPaths) }
        get() = LazyValue.of({ _path }, { _path = it }) { computeNormalizedPath(subPaths) }
    @Volatile private var _path: String? = null // endregion
    override val subPaths: List<String> = computeNormalizedSubPaths(input.subPaths)
}

private object EmptyCwtConfigPath : CwtConfigPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

// endregion

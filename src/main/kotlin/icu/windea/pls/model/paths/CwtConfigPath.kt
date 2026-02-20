package icu.windea.pls.model.paths

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.collections.ImmutableList
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.core.splitFast

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
interface CwtConfigPath : Iterable<String> {
    val path: String
    val subPaths: List<String> // 子路径中不用保留括起的双引号
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()
    fun normalize(): CwtConfigPath = this

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): CwtConfigPath
        fun resolve(input: String): CwtConfigPath
        fun resolve(input: List<String>): CwtConfigPath
    }

    companion object : Resolver by CwtConfigPathResolverImpl()
}

@Suppress("unused")
fun CwtConfigPath.relativeTo(other: CwtConfigPath): CwtConfigPath? {
    if (this == other) return CwtConfigPath.resolveEmpty()
    if (this.isEmpty()) return other
    val subPaths = other.subPaths.removePrefixOrNull(this.subPaths) ?: return null
    return CwtConfigPath.resolve(subPaths)
}

// region Implementations

private val stringInterner = Interner.newWeakInterner<String>()

private fun String.internString() = stringInterner.intern(this)
private fun String.splitSubPaths() = replace("\\/", "\u0000").splitFast('/').mapFast { it.replace('\u0000', '/') }
private fun List<String>.joinSubPaths() = joinToString("/") { it.replace("/", "\\/") }

private class CwtConfigPathResolverImpl : CwtConfigPath.Resolver {
    override fun resolveEmpty(): CwtConfigPath = EmptyCwtConfigPath

    override fun resolve(input: String): CwtConfigPath {
        if (input.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImplFromPath(input)
    }

    override fun resolve(input: List<String>): CwtConfigPath {
        if (input.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImplFromSubPaths(input)
    }
}

private sealed class CwtConfigPathBase : CwtConfigPath {
    override val length: Int get() = subPaths.size

    override fun normalize(): CwtConfigPath {
        if (this is NormalizedCwtConfigPath || this is EmptyCwtConfigPath) return this
        if (this.isEmpty()) return EmptyCwtConfigPath
        return NormalizedCwtConfigPath(this)
    }

    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class CwtConfigPathImplFromPath(input: String) : CwtConfigPathBase() {
    override val path: String = input
    override val subPaths: List<String> = input.splitSubPaths()
}

private class CwtConfigPathImplFromSubPaths(input: List<String>) : CwtConfigPathBase() {
    override val path: String = input.joinSubPaths()
    override val subPaths: List<String> = input
}

private class NormalizedCwtConfigPath(input: CwtConfigPath) : CwtConfigPathBase() {
    override val path: String = input.path.internString()
    override val subPaths: List<String> = ImmutableList(input.subPaths.size) { input.subPaths[it].internString() }
}

private object EmptyCwtConfigPath : CwtConfigPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

// endregion

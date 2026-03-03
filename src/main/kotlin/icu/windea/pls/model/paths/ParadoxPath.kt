package icu.windea.pls.model.paths

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.collections.ImmutableList
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.splitFast

/**
 * 游戏或模组文件的路径。
 *
 * 说明：
 * - 相对于游戏或模组入口目录（不一定是根目录）。
 * - 使用 "/" 分隔子路径。
 * - 保留大小写。
 *
 * 示例：
 * - `common/buildings/00_capital_buildings.txt`
 * - `localisation/simp_chinese/l_simp_chinese.yml`
 */
interface ParadoxPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val parent: String
    val root: String
    val fileName: String
    val fileExtension: String?
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()
    fun normalize(): ParadoxPath = this

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): ParadoxPath
        fun resolve(input: String): ParadoxPath
        fun resolve(input: List<String>): ParadoxPath
    }

    companion object : Resolver by ParadoxPathResolverImpl()
}

fun ParadoxPath.matchesParent(path: String, strict: Boolean = false): Boolean {
    return if (strict) this.parent == path else path.matchesPath(this.path)
}

fun ParadoxPath.matchesExtension(extension: String): Boolean {
    return this.fileExtension?.lowercase() == extension
}

fun ParadoxPath.matchesExtensions(extensions: Array<String>): Boolean {
    return this.fileExtension?.lowercase() in extensions
}

@Suppress("unused")
fun ParadoxPath.matchesExtensions(extensions: Collection<String>): Boolean {
    return this.fileExtension?.lowercase() in extensions
}

// region Implementations

private val stringInterner = Interner.newWeakInterner<String>()

private fun String.internString() = stringInterner.intern(this)
private fun String.splitSubPaths() = splitFast('/')
private fun List<String>.joinSubPaths() = joinToString("/")
private fun String.getParent() = substringBeforeLast('/', "")

private class ParadoxPathResolverImpl : ParadoxPath.Resolver {
    override fun resolveEmpty(): ParadoxPath = EmptyParadoxPath

    override fun resolve(input: String): ParadoxPath {
        if (input.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImplFromPath(input)
    }

    override fun resolve(input: List<String>): ParadoxPath {
        if (input.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImplFromSubPaths(input)
    }
}

private sealed class ParadoxPathBase : ParadoxPath {
    override val root: String get() = subPaths.firstOrNull().orEmpty()
    override val fileName: String get() = subPaths.lastOrNull().orEmpty()
    override val fileExtension: String? get() = fileName.substringAfterLast('.', "").orNull()
    override val length: Int get() = subPaths.size

    override fun normalize(): ParadoxPath {
        if (this is NormalizedParadoxPath || this is EmptyParadoxPath) return this
        if (this.isEmpty()) return EmptyParadoxPath
        return NormalizedParadoxPath(this)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxPathImplFromPath(input: String) : ParadoxPathBase() {
    override val path: String = input
    override val subPaths: List<String> = input.splitSubPaths()
    override val parent: String = input.getParent()
}

private class ParadoxPathImplFromSubPaths(input: List<String>) : ParadoxPathBase() {
    override val path: String = input.joinSubPaths()
    override val subPaths: List<String> = input
    override val parent: String = path.getParent()
}

private class NormalizedParadoxPath(input: ParadoxPath) : ParadoxPathBase() {
    override val path: String = input.path.internString()
    override val subPaths: List<String> = ImmutableList(input.subPaths.size) { input.subPaths[it].internString() }
    override val parent: String = if (input.length == 1) "" else input.parent.internString()
}

private object EmptyParadoxPath : ParadoxPathBase() {
    override val subPaths: List<String> get() = emptyList()
    override val path: String get() = ""
    override val parent: String get() = ""
    override val root: String get() = ""
    override val fileName: String get() = ""
    override val fileExtension: String? get() = null
}

// endregion

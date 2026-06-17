@file:Optimized

package icu.windea.pls.model.paths

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.ImmutableList
import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.splitFast
import icu.windea.pls.model.constraints.ParadoxPathConstraint

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
 *
 * @see ParadoxPathConstraint
 */
interface ParadoxPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val parent: String
    val root: String
    val fileName: String
    val fileExtension: String?
    val length: Int

    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun get(index: Int): String

    fun normalize(): ParadoxPath
    fun resolve(other: ParadoxPath): ParadoxPath?
    fun relativize(other: ParadoxPath, wildcard: String? = null): ParadoxPath?

    fun matchesParent(path: String, strict: Boolean = false): Boolean
    fun matchesExtension(extension: String): Boolean
    fun matchesExtensions(extensions: Array<String>): Boolean
    fun matchesExtensions(extensions: Collection<String>): Boolean

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        @JvmStatic
        fun resolveEmpty(): ParadoxPath = ParadoxPathResolver.resolveEmpty()

        @JvmStatic
        fun resolve(input: String): ParadoxPath = ParadoxPathResolver.resolve(input)

        @JvmStatic
        fun resolve(input: List<String>): ParadoxPath = ParadoxPathResolver.resolve(input)
    }
}

// region Implementations

private val pathInterner = Interner.newWeakInterner<String>()
private val subPathInterner = Interner.newWeakInterner<String>()

private fun String.internPath() = pathInterner.intern(this)
private fun String.internSubPath() = subPathInterner.intern(this)
private fun String.splitSubPaths() = splitFast('/')
private fun List<String>.joinSubPaths() = joinToString("/")
private fun String.getParent() = substringBeforeLast('/', "")

private object ParadoxPathResolver {
    fun resolveEmpty(): ParadoxPath = EmptyParadoxPath

    fun resolve(input: String): ParadoxPath {
        if (input.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImplFromPath(input)
    }

    fun resolve(input: List<String>): ParadoxPath {
        if (input.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImplFromSubPaths(input)
    }
}

private sealed class ParadoxPathBase : ParadoxPath {
    override val root: String get() = subPaths.firstOrNull().orEmpty()
    override val fileName: String get() = subPaths.lastOrNull().orEmpty()
    override val fileExtension: String? get() = fileName.substringAfterLast('.', "").orNull()
    override val length: Int get() = subPaths.size

    override fun isEmpty(): Boolean = length == 0

    override fun isNotEmpty(): Boolean = length != 0

    override fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()

    override fun normalize(): ParadoxPath {
        if (this is NormalizedParadoxPath || this is EmptyParadoxPath) return this
        if (this.isEmpty()) return EmptyParadoxPath
        return NormalizedParadoxPath(this)
    }

    override fun resolve(other: ParadoxPath): ParadoxPath {
        if (other.isEmpty()) return this
        val subPaths = this.subPaths + other.subPaths
        return ParadoxPath.resolve(subPaths)
    }

    override fun relativize(other: ParadoxPath, wildcard: String?): ParadoxPath? {
        if (this == other) return ParadoxPath.resolveEmpty()
        if (this.isEmpty()) return other
        val subPaths = other.subPaths.removePrefixOrNull(this.subPaths, wildcard) ?: return null
        return ParadoxPath.resolve(subPaths)
    }

    override fun matchesParent(path: String, strict: Boolean): Boolean {
        return if (strict) this.parent == path else path.matchesPath(this.path)
    }

    override fun matchesExtension(extension: String): Boolean {
        return this.fileExtension?.lowercase() == extension
    }

    override fun matchesExtensions(extensions: Array<String>): Boolean {
        return this.fileExtension?.lowercase() in extensions
    }

    override fun matchesExtensions(extensions: Collection<String>): Boolean {
        return this.fileExtension?.lowercase() in extensions
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
    override val path: String = if (input.length == 1) input.path.internSubPath() else input.path.internPath()
    override val subPaths: List<String> = ImmutableList(input.subPaths.size) { input.subPaths[it].internSubPath() }
    override val parent: String = if (input.length == 1) "" else input.parent.internPath()
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

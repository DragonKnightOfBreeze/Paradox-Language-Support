@file:Optimized

package icu.windea.pls.model.paths.impl

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.splitFast
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.model.paths.ParadoxPath

private val stringInterner = Interner.newWeakInterner<String>()

private fun String.internPath() = stringInterner.intern(this)
private fun String.splitSubPaths() = splitFast('/')
private fun List<String>.joinSubPaths() = joinToString("/")
private fun String.getParent() = substringBeforeLast('/', "")

internal class ParadoxPathResolverImpl : ParadoxPath.Resolver {
    override fun resolveEmpty(): ParadoxPath = EmptyParadoxPath

    override fun resolve(input: String): ParadoxPath {
        if (input.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImplFromPath(input)
    }

    override fun resolve(input: List<String>): ParadoxPath {
        if (input.isEmpty()) return EmptyParadoxPath
        if (input.size == 1) return NormalizedParadoxPath(input.get(0), true)
        return ParadoxPathImplFromSubPaths(input)
    }
}

private abstract class ParadoxPathBase : ParadoxPath {
    override val root: String get() = subPaths.firstOrNull().orEmpty()
    override val fileName: String get() = subPaths.lastOrNull().orEmpty()
    override val fileExtension: String? get() = fileName.substringAfterLast('.', "").orNull()
    override val length: Int get() = subPaths.size

    override fun normalize(): ParadoxPath {
        if (this is NormalizedParadoxPath || this is EmptyParadoxPath) return this
        if (this.isEmpty()) return EmptyParadoxPath
        if (this.subPaths.size == 1) return NormalizedParadoxPath(subPaths.get(0), true)
        return NormalizedParadoxPath(path, false)
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

private class NormalizedParadoxPath(input: String, singleton: Boolean) : ParadoxPathBase() {
    override val path: String = input.internPath()
    override val subPaths: List<String> = if (singleton) path.singleton.list() else path.splitSubPaths().map { it.internPath() }.optimized()
    override val parent: String = if (singleton) "" else path.getParent().internPath()
}

private object EmptyParadoxPath : ParadoxPathBase() {
    override val subPaths: List<String> get() = emptyList()
    override val path: String get() = ""
    override val parent: String get() = ""
    override val root: String get() = ""
    override val fileName: String get() = ""
    override val fileExtension: String? get() = null
}

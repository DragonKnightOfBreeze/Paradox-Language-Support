package icu.windea.pls.model.paths.impl

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.splitFast
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.model.paths.ParadoxElementPath

private val interner = Interner.newWeakInterner<String>()
private val cacheForSingleton = CacheBuilder().build<String, ParadoxElementPath> { OptimizedParadoxElementPath(it, true) }
private val cache = CacheBuilder().build<String, ParadoxElementPath> { OptimizedParadoxElementPath(it, false) }

private fun String.internPath() = interner.intern(this)
private fun String.splitSubPaths() = replace("\\/", "\u0000").splitFast('/').map { it.replace('\u0000', '/') }
private fun List<String>.joinSubPaths() = joinToString("/") { it.replace("/", "\\/") }

internal class ParadoxElementPathResolverImpl : ParadoxElementPath.Resolver {
    override fun resolveEmpty(): ParadoxElementPath = EmptyParadoxElementPath

    override fun resolve(input: String): ParadoxElementPath {
        if (input.isEmpty()) return EmptyParadoxElementPath
        return ParadoxElementPathImpl1(input)
    }

    override fun resolve(input: List<String>): ParadoxElementPath {
        if (input.isEmpty()) return EmptyParadoxElementPath
        if (input.size == 1) return cacheForSingleton.get(input.get(0))
        return ParadoxElementPathImpl2(input)
    }

    override fun invalidateCache() {
        cacheForSingleton.invalidateAll()
        cache.invalidateAll()
    }
}

private abstract class ParadoxElementPathBase : ParadoxElementPath {
    override val length: Int get() = subPaths.size

    override fun normalize(): ParadoxElementPath {
        if (this is OptimizedParadoxElementPath || this is EmptyParadoxElementPath) return this
        if (this.isEmpty()) return EmptyParadoxElementPath
        if (this.subPaths.size == 1) return cacheForSingleton.get(this.subPaths.get(0))
        return cache.get(this.path)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxElementPathImpl1(input: String) : ParadoxElementPathBase() {
    override val path: String = input
    override val subPaths: List<String> = input.splitSubPaths()
}

private class ParadoxElementPathImpl2(input: List<String>) : ParadoxElementPathBase() {
    override val path: String = input.joinSubPaths()
    override val subPaths: List<String> = input
}

private class OptimizedParadoxElementPath(input: String, singleton: Boolean) : ParadoxElementPathBase() {
    override val path: String = input.internPath()
    override val subPaths: List<String> = if (singleton) listOf(path) else path.splitSubPaths().map { it.internPath() }
}

private object EmptyParadoxElementPath : ParadoxElementPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

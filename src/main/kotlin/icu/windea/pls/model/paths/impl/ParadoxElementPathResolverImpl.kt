package icu.windea.pls.model.paths.impl

import icu.windea.pls.core.splitFast
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.model.paths.ParadoxElementPath

private val stringPool = CacheBuilder()
    .build<String, String> { it }
private val cacheForSingleton = CacheBuilder()
    .build<String, ParadoxElementPath> { OptimizedParadoxElementPath(it, true) }
private val cache = CacheBuilder()
    .build<String, ParadoxElementPath> { OptimizedParadoxElementPath(it, false) }

private fun String.optimized() = stringPool.get(this)
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
        stringPool.invalidateAll()
        cacheForSingleton.invalidateAll()
        cache.invalidateAll()
    }
}

private abstract class ParadoxElementPathBase : ParadoxElementPath {
    override val length: Int get() = subPaths.size

    override fun optimized(): ParadoxElementPath {
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
    override val path: String = input.optimized()
    override val subPaths: List<String> = if (singleton) listOf(path) else path.splitSubPaths().map { it.optimized() }
}

private object EmptyParadoxElementPath : ParadoxElementPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

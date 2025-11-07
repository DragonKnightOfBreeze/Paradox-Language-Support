package icu.windea.pls.model.paths.impl

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.splitFast
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.model.paths.CwtConfigPath

private val interner = Interner.newWeakInterner<String>()
private val cacheForSingleton = CacheBuilder().build<String, CwtConfigPath> { NormalizedCwtConfigPath(it, true) }
private val cache = CacheBuilder().build<String, CwtConfigPath> { NormalizedCwtConfigPath(it, false) }

private fun String.internPath() = interner.intern(this)
private fun String.splitSubPaths() = replace("\\/", "\u0000").splitFast('/').map { it.replace('\u0000', '/') }
private fun List<String>.joinSubPaths() = joinToString("/") { it.replace("/", "\\/") }

internal class CwtConfigPathResolverImpl : CwtConfigPath.Resolver {
    override fun resolveEmpty(): CwtConfigPath = EmptyCwtConfigPath

    override fun resolve(input: String): CwtConfigPath {
        if (input.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImplFromPath(input)
    }

    override fun resolve(input: List<String>): CwtConfigPath {
        if (input.isEmpty()) return EmptyCwtConfigPath
        if (input.size == 1) return cacheForSingleton.get(input.get(0))
        return CwtConfigPathImplFromSubPaths(input)
    }

    override fun invalidateCache() {
        cacheForSingleton.invalidateAll()
        cache.invalidateAll()
    }
}

private abstract class CwtConfigPathBase : CwtConfigPath {
    override val length: Int get() = subPaths.size

    override fun normalize(): CwtConfigPath {
        if (this is NormalizedCwtConfigPath || this is EmptyCwtConfigPath) return this
        if (this.isEmpty()) return EmptyCwtConfigPath
        if (this.subPaths.size == 1) return cacheForSingleton.get(this.subPaths.get(0))
        return cache.get(this.path)
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

private class NormalizedCwtConfigPath(input: String, singleton: Boolean) : CwtConfigPathBase() {
    override val path: String = input.internPath()
    override val subPaths: List<String> = if (singleton) listOf(path) else path.splitSubPaths().map { it.internPath() }
}

private object EmptyCwtConfigPath : CwtConfigPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

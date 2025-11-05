package icu.windea.pls.model.paths.impl

import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.model.paths.ParadoxElementPath

internal class ParadoxElementPathResolverImpl : ParadoxElementPath.Resolver {
    private val cacheForSingleton = CacheBuilder("maximumSize=4096, expireAfterAccess=30m")
        .build<String, ParadoxElementPath> { SingletonParadoxElementPath(it) }

    override fun resolveEmpty(): ParadoxElementPath = EmptyParadoxElementPath

    override fun resolve(path: String): ParadoxElementPath {
        if (path.isEmpty()) return EmptyParadoxElementPath
        return ParadoxElementPathImpl1(path)
    }

    override fun resolve(subPaths: List<String>): ParadoxElementPath {
        if (subPaths.isEmpty()) return EmptyParadoxElementPath
        if (subPaths.size == 1) return SingletonParadoxElementPath(subPaths.get(0))
        return ParadoxElementPathImpl2(subPaths)
    }

    override fun intern(input: ParadoxElementPath): ParadoxElementPath {
        return when {
            input.subPaths.isEmpty() -> EmptyParadoxElementPath
            input.subPaths.size == 1 -> cacheForSingleton.get(input.subPaths.get(0))
            else -> input
        }
    }
}

// - 不要在创建时使用 `String.intern()`
// - 优化子路径列表为空的情况
// - 优化子路径列表为单例的情况

private abstract class ParadoxElementPathImpl : ParadoxElementPath {
    override val length: Int get() = subPaths.size

    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxElementPathImpl1(path: String) : ParadoxElementPathImpl() {
    override val path: String = path
    override val subPaths: List<String> = path.replace("\\/", "\u0000").split('/').map { it.replace('\u0000', '/') }.optimized()
}

private class ParadoxElementPathImpl2(subPaths: List<String>) : ParadoxElementPathImpl() {
    override val path: String = subPaths.joinToString("/") { it.replace("/", "\\/") }
    override val subPaths: List<String> = subPaths
}

private class SingletonParadoxElementPath(subPath: String) : ParadoxElementPathImpl() {
    override val path: String = subPath.replace("/", "\\/")
    override val subPaths: List<String> = listOf(subPath)
}

private object EmptyParadoxElementPath : ParadoxElementPathImpl() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

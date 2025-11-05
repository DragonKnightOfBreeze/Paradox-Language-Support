package icu.windea.pls.model.paths.impl

import icu.windea.pls.core.collections.optimized
import icu.windea.pls.model.paths.CwtConfigPath

internal class CwtConfigPathResolverImpl : CwtConfigPath.Resolver {
    override fun resolveEmpty(): CwtConfigPath = EmptyCwtConfigPath

    override fun resolve(path: String): CwtConfigPath {
        if (path.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImpl1(path)
    }

    override fun resolve(subPaths: List<String>): CwtConfigPath {
        if (subPaths.isEmpty()) return EmptyCwtConfigPath
        if (subPaths.size == 1) return SingletonCwtConfigPath(subPaths.get(0))
        return CwtConfigPathImpl2(subPaths)
    }
}

// - 不要在创建时使用 `String.intern()`
// - 优化子路径列表为空的情况
// - 优化子路径列表为单例的情况

private abstract class CwtConfigPathBase : CwtConfigPath {
    override val length: Int get() = subPaths.size

    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class CwtConfigPathImpl1(path: String) : CwtConfigPathBase() {
    override val path: String = path
    override val subPaths: List<String> = path.replace("\\/", "\u0000").split('/').map { it.replace('\u0000', '/') }.optimized()
}

private class CwtConfigPathImpl2(subPaths: List<String>) : CwtConfigPathBase() {
    override val path: String = subPaths.joinToString("/") { it.replace("/", "\\/") }
    override val subPaths: List<String> = subPaths
}

private class SingletonCwtConfigPath(subPath: String) : CwtConfigPathBase() {
    override val path: String = subPath.replace("/", "\\/")
    override val subPaths: List<String> = listOf(subPath)
}

private object EmptyCwtConfigPath : CwtConfigPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

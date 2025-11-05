package icu.windea.pls.model.paths.impl

import icu.windea.pls.core.orNull
import icu.windea.pls.model.paths.ParadoxPath

internal class ParadoxPathResolverImpl : ParadoxPath.Resolver {
    override fun resolveEmpty(): ParadoxPath = EmptyParadoxPath

    override fun resolve(path: String): ParadoxPath {
        if (path.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImpl1(path)
    }

    override fun resolve(subPaths: List<String>): ParadoxPath {
        if (subPaths.isEmpty()) return EmptyParadoxPath
        return ParadoxPathImpl2(subPaths)
    }
}

// - 不要在创建时使用 `String.intern()`
// - 优化子路径列表为空的情况

private abstract class ParadoxPathBase : ParadoxPath {
    override val root: String get() = subPaths.firstOrNull().orEmpty()
    override val fileName: String get() = subPaths.lastOrNull().orEmpty()
    override val fileExtension: String? get() = fileName.substringAfterLast('.', "").orNull()
    override val length: Int get() = subPaths.size

    override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxPathImpl1(path: String) : ParadoxPathBase() {
    override val path: String = path
    override val subPaths: List<String> = path.split('/')
    override val parent: String = path.substringBeforeLast('/', "")
}

private class ParadoxPathImpl2(subPaths: List<String>) : ParadoxPathBase() {
    override val path: String = subPaths.joinToString("/")
    override val subPaths: List<String> = subPaths
    override val parent: String = path.substringBeforeLast('/', "")
}

private object EmptyParadoxPath : ParadoxPathBase() {
    override val subPaths: List<String> get() = emptyList()
    override val path: String get() = ""
    override val parent: String get() = ""
    override val root: String get() = ""
    override val fileName: String get() = ""
    override val fileExtension: String? get() = null
}

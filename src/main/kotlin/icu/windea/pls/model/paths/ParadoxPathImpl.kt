package icu.windea.pls.model.paths

import icu.windea.pls.core.orNull

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

private sealed class ParadoxPathImpl : ParadoxPath {
    override val root: String get() = subPaths.firstOrNull().orEmpty()
    override val fileName: String get() = subPaths.lastOrNull().orEmpty()
    override val fileExtension: String? get() = fileName.substringAfterLast('.', "").orNull()
    override val length: Int get() = subPaths.size

    override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxPathImpl1(path: String) : ParadoxPathImpl() {
    override val path: String = path
    override val subPaths: List<String> = path.split('/')
    override val parent: String = path.substringBeforeLast('/', "")
}

private class ParadoxPathImpl2(subPaths: List<String>) : ParadoxPathImpl() {
    override val path: String = subPaths.joinToString("/")
    override val subPaths: List<String> = subPaths
    override val parent: String = path.substringBeforeLast('/', "")
}

private object EmptyParadoxPath : ParadoxPath {
    override val subPaths: List<String> = emptyList()
    override val path: String = ""
    override val parent: String = ""
    override val root: String = ""
    override val fileName: String = ""
    override val fileExtension: String? = null
    override val length: Int = 0

    override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

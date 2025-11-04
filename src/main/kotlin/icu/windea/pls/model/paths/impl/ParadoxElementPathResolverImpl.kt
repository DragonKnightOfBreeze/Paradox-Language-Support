package icu.windea.pls.model.paths.impl

import icu.windea.pls.core.collections.optimized
import icu.windea.pls.model.paths.ParadoxElementPath

internal class ParadoxElementPathResolverImpl : ParadoxElementPath.Resolver {
    override fun resolveEmpty(): ParadoxElementPath = EmptyParadoxElementPath

    override fun resolve(path: String): ParadoxElementPath {
        if (path.isEmpty()) return EmptyParadoxElementPath
        return ParadoxElementPathImpl(path)
    }

    override fun resolve(subPaths: List<String>): ParadoxElementPath {
        if (subPaths.isEmpty()) return EmptyParadoxElementPath
        return ParadoxElementPathImpl(subPaths)
    }
}

private class ParadoxElementPathImpl : ParadoxElementPath {
    override val path: String
    override val subPaths: List<String>
    override val length: Int get() = subPaths.size

    constructor(path: String) {
        this.path = getPath(path)
        this.subPaths = getSubPaths(path)
    }

    constructor(subPaths: List<String>) {
        this.path = getPath(subPaths)
        this.subPaths = getSubPath(subPaths)
    }

    private fun getPath(path: String): String {
        // intern to optimize memory
        return path.intern()
    }

    private fun getPath(subPaths: List<String>): String {
        // use simple implementation & intern to optimize memory
        return subPaths.joinToString("/") { it.replace("/", "\\/") }.intern()
    }

    private fun getSubPaths(path: String): List<String> {
        // use simple implementation & intern and optimized to optimize memory
        return path.replace("\\/", "\u0000").split('/').map { it.replace('\u0000', '/').intern() }.optimized()
    }

    private fun getSubPath(subPaths: List<String>): List<String> {
        // optimized to optimize memory
        return subPaths.optimized()
    }

    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private object EmptyParadoxElementPath : ParadoxElementPath {
    override val path: String = ""
    override val subPaths: List<String> = emptyList()
    override val length: Int = 0

    override fun equals(other: Any?) = this === other || other is ParadoxElementPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

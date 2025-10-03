package icu.windea.pls.model.paths.impl

import icu.windea.pls.model.paths.CwtConfigPath
import icu.windea.pls.model.paths.ParadoxExpressionPath

internal class ParadoxExpressionPathResolverImpl : ParadoxExpressionPath.Resolver {
    override fun resolveEmpty(): ParadoxExpressionPath = EmptyParadoxExpressionPath

    override fun resolve(path: String): ParadoxExpressionPath {
        if (path.isEmpty()) return EmptyParadoxExpressionPath
        return ParadoxExpressionPathImpl(path)
    }

    override fun resolve(subPaths: List<String>): ParadoxExpressionPath {
        if (subPaths.isEmpty()) return EmptyParadoxExpressionPath
        return ParadoxExpressionPathImpl(subPaths)
    }
}

private class ParadoxExpressionPathImpl : ParadoxExpressionPath {
    override val path: String
    override val subPaths: List<String>
    override val length: Int get() = subPaths.size

    constructor(path: String) {
        this.path = path
        this.subPaths = path2SubPaths(path)
    }

    constructor(subPaths: List<String>) {
        this.path = subPaths2Path(subPaths)
        this.subPaths = subPaths
    }

    private fun path2SubPaths(path: String): List<String> {
        // use simple implementation
        return path.replace("\\/", "\u0000").split('/').map { it.replace('\u0000', '/') }
    }

    private fun subPaths2Path(subPaths: List<String>): String {
        // use simple implementation
        return subPaths.joinToString("/") { it.replace("/", "\\/") }
    }

    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private object EmptyParadoxExpressionPath : ParadoxExpressionPath {
    override val path: String = ""
    override val subPaths: List<String> = emptyList()
    override val length: Int = 0

    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

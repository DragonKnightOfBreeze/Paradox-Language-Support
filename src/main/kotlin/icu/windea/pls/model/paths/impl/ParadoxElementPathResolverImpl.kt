@file:Optimized

package icu.windea.pls.model.paths.impl

import com.github.benmanes.caffeine.cache.Interner
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.splitFast
import icu.windea.pls.model.paths.ParadoxMemberPath

private val stringInterner = Interner.newWeakInterner<String>()

private fun String.internPath() = stringInterner.intern(this)
private fun String.splitSubPaths() = replace("\\/", "\u0000").splitFast('/').map { it.replace('\u0000', '/') }
private fun List<String>.joinSubPaths() = joinToString("/") { it.replace("/", "\\/") }

internal class ParadoxElementPathResolverImpl : ParadoxMemberPath.Resolver {
    override fun resolveEmpty(): ParadoxMemberPath = EmptyParadoxMemberPath

    override fun resolve(input: String): ParadoxMemberPath {
        if (input.isEmpty()) return EmptyParadoxMemberPath
        return ParadoxMemberPathImplFromPath(input)
    }

    override fun resolve(input: List<String>): ParadoxMemberPath {
        if (input.isEmpty()) return EmptyParadoxMemberPath
        return ParadoxMemberPathImplFromSubPaths(input)
    }
}

private abstract class ParadoxMemberPathBase : ParadoxMemberPath {
    override val length: Int get() = subPaths.size

    override fun normalize(): ParadoxMemberPath {
        if (this is NormalizedParadoxMemberPath || this is EmptyParadoxMemberPath) return this
        if (this.isEmpty()) return EmptyParadoxMemberPath
        return NormalizedParadoxMemberPath(this)
    }

    override fun equals(other: Any?) = this === other || other is ParadoxMemberPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxMemberPathImplFromPath(input: String) : ParadoxMemberPathBase() {
    override val path: String = input
    override val subPaths: List<String> = input.splitSubPaths()
}

private class ParadoxMemberPathImplFromSubPaths(input: List<String>) : ParadoxMemberPathBase() {
    override val path: String = input.joinSubPaths()
    override val subPaths: List<String> = input
}

private class NormalizedParadoxMemberPath(input: ParadoxMemberPath) : ParadoxMemberPathBase() {
    override val path: String = input.path.internPath()
    override val subPaths: List<String> = input.subPaths.map { it.internPath() }.optimized()
}

private object EmptyParadoxMemberPath : ParadoxMemberPathBase() {
    override val path: String get() = ""
    override val subPaths: List<String> get() = emptyList()
}

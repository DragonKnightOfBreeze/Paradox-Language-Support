package icu.windea.pls.model.paths

import icu.windea.pls.core.*

internal class ParadoxExpressionPathResolverImpl : ParadoxExpressionPath.Resolver {
    override fun resolveEmpty(): ParadoxExpressionPath = EmptyParadoxExpressionPath

    override fun resolve(originalPath: String): ParadoxExpressionPath {
        if (originalPath.isEmpty()) return EmptyParadoxExpressionPath
        return ParadoxExpressionPathImpl1(originalPath)
    }

    override fun resolve(originalSubPaths: List<String>): ParadoxExpressionPath {
        if (originalSubPaths.isEmpty()) return EmptyParadoxExpressionPath
        val mayBeQuoted = originalSubPaths.any { it.isQuoted('"') }
        if (mayBeQuoted) return ParadoxExpressionPathImpl2(originalSubPaths)
        return ParadoxExpressionPathImpl3(originalSubPaths)
    }
}

private sealed class ParadoxExpressionPathImpl : ParadoxExpressionPath {
    override val length: Int get() = originalSubPaths.size

    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && originalPath == other.originalPath
    override fun hashCode() = originalPath.hashCode()
    override fun toString() = originalPath

    protected fun path2SubPaths(path: String): List<String> {
        return buildList {
            val builder = StringBuilder()
            var escape = false
            path.forEach { c ->
                when {
                    c == '\\' -> {
                        escape = true
                    }
                    c == '/' && !escape -> {
                        if (builder.isNotEmpty()) add(builder.toString())
                        builder.clear()
                    }
                    else -> {
                        if (escape) escape = false
                        builder.append(c)
                    }
                }
            }
            if (builder.isNotEmpty()) add(builder.toString())
        }
    }

    protected fun subPaths2Path(subPaths: List<String>): String {
        val builder = StringBuilder()
        var isFirst = true
        subPaths.forEach { p ->
            if (isFirst) isFirst = false else builder.append('/')
            p.forEach { c ->
                if (c == '/') builder.append('\\')
                builder.append(c)
            }
        }
        return builder.toString()
    }
}

private class ParadoxExpressionPathImpl1(originalPath: String) : ParadoxExpressionPathImpl() {
    override val subPaths: List<String> by lazy { originalSubPaths.map { it.unquote() } }
    override val path: String by lazy { subPaths2Path(subPaths) }
    override val originalPath: String = originalPath
    override val originalSubPaths: List<String> by lazy { path2SubPaths(originalPath) }
}

private class ParadoxExpressionPathImpl2(originalSubPaths: List<String>) : ParadoxExpressionPathImpl() {
    override val subPaths: List<String> by lazy { originalSubPaths.map { it.unquote() } }
    override val path: String by lazy { subPaths2Path(subPaths) }
    override val originalPath: String by lazy { subPaths2Path(originalSubPaths) }
    override val originalSubPaths: List<String> = originalSubPaths
}

private class ParadoxExpressionPathImpl3(originalSubPaths: List<String>) : ParadoxExpressionPathImpl() {
    override val path: String get() = originalPath
    override val subPaths: List<String> get() = originalSubPaths
    override val originalPath: String by lazy { subPaths2Path(originalSubPaths) }
    override val originalSubPaths: List<String> = originalSubPaths
}

private object EmptyParadoxExpressionPath : ParadoxExpressionPath {
    override val path: String = ""
    override val subPaths: List<String> = emptyList()
    override val originalPath: String = ""
    override val originalSubPaths: List<String> = emptyList()
    override val length: Int = 0

    override fun equals(other: Any?) = this === other || other is ParadoxExpressionPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

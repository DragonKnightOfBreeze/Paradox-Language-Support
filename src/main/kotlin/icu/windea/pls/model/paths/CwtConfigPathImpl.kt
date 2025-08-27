package icu.windea.pls.model.paths

internal class CwtConfigPathResolverImpl : CwtConfigPath.Resolver {
    override fun resolveEmpty(): CwtConfigPath = EmptyCwtConfigPath

    override fun resolve(path: String): CwtConfigPath {
        if (path.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImpl(path)
    }

    override fun resolve(subPaths: List<String>): CwtConfigPath {
        if (subPaths.isEmpty()) return EmptyCwtConfigPath
        return CwtConfigPathImpl(subPaths)
    }
}

private class CwtConfigPathImpl : CwtConfigPath {
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
        //use simple implementation
        return path.replace("\\/", "\u0000").split('/').map { it.replace('\u0000', '/') }
    }

    private fun subPaths2Path(subPaths: List<String>): String {
        //use simple implementation
        return subPaths.joinToString("/") { it.replace("/", "\\/") }
    }

    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private object EmptyCwtConfigPath : CwtConfigPath {
    override val path: String = ""
    override val subPaths: List<String> = emptyList()
    override val length: Int = 0

    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

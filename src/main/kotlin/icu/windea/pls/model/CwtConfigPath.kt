package icu.windea.pls.model

/**
 * CWT规则的路径。保留大小写。忽略括起的双引号。
 */
interface CwtConfigPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()

    override fun iterator(): Iterator<String> = subPaths.iterator()

    companion object Resolver {
        val Empty: CwtConfigPath = EmptyCwtConfigPath

        fun resolve(path: String): CwtConfigPath = doResolve(path)

        fun resolve(subPaths: List<String>): CwtConfigPath = doResolve(subPaths)
    }
}

//Implementations (not interned)

private fun doResolve(path: String): CwtConfigPath {
    if (path.isEmpty()) return EmptyCwtConfigPath
    return CwtConfigPathImpl(path)
}

private fun doResolve(subPaths: List<String>): CwtConfigPath {
    if (subPaths.isEmpty()) return EmptyCwtConfigPath
    return CwtConfigPathImpl(subPaths)
}

//12 + 2 * 4 = 20 -> 24
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

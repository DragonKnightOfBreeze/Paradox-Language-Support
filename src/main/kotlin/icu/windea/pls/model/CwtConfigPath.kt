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
    fun get(index: Int): String = this.subPaths.getOrNull(index).orEmpty()
    
    override fun iterator(): Iterator<String> = this.subPaths.iterator()
    
    companion object Resolver {
        val Empty: CwtConfigPath = EmptyCwtConfigPath
        
        fun resolve(subPaths: List<String>): CwtConfigPath = doResolve(subPaths)
    }
}

//Implementations (interned)

private fun doResolve(subPaths: List<String>): CwtConfigPath {
    if(subPaths.isEmpty()) return EmptyCwtConfigPath
    return CwtConfigPathImpl(subPaths)
}

private class CwtConfigPathImpl(
    subPaths: List<String>
) : CwtConfigPath {
    override val path: String = subPaths.joinToString("/") { it.replace("/", "\\/") }.intern()
    override val subPaths: List<String> = subPaths.map { it.intern() }
    override val length: Int = subPaths.size
    
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

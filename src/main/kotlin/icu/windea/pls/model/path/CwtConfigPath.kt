package icu.windea.pls.model.path

import com.google.common.cache.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

/**
 * CWT规则的领。保留大小写。忽略括起的双引号。
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
        
        fun resolve(path: String): CwtConfigPath = cache.get(path)
    }
}

//Implementations (cached & interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, _> { doResolve(it) }

private fun doResolve(path: String): CwtConfigPath {
    if(path.isEmpty()) return EmptyCwtConfigPath
    return CwtConfigPathImpl(path)
}

private class CwtConfigPathImpl(
    path: String
) : CwtConfigPath {
    override val path: String = path.intern()
    override val subPaths: List<String> = path.split('/').mapFast { it.intern() }
    override val length: Int = subPaths.size
    
    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private object EmptyCwtConfigPath: CwtConfigPath {
    override val path: String = ""
    override val subPaths: List<String> = emptyList()
    override val length: Int = 0
    
    override fun equals(other: Any?) = this === other || other is CwtConfigPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}
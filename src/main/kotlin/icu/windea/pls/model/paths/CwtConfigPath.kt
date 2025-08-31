package icu.windea.pls.model.paths

import icu.windea.pls.model.paths.impl.CwtConfigPathResolverImpl

/**
 * CWT 规则的路径。保留大小写。忽略括起的双引号。
 */
interface CwtConfigPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()

    override fun iterator(): Iterator<String> = subPaths.iterator()

    interface Resolver {
        fun resolveEmpty(): CwtConfigPath
        fun resolve(path: String): CwtConfigPath
        fun resolve(subPaths: List<String>): CwtConfigPath
    }

    companion object : Resolver by CwtConfigPathResolverImpl()
}

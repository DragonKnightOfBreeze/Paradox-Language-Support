package icu.windea.pls.model.paths

import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.model.paths.impl.CwtConfigPathResolverImpl

/**
 * 规则在规则文件中的路径。
 *
 * 说明：
 * - 相对于规则文件，或者其他成员规则。
 * - 如果对应深度的成员规则是属性规则，则对应的子路径需要匹配其键。如果是单独的值规则，则需要是 `-`。
 * - 使用 "/" 分隔子路径。如果子路径中存在 "/"，会先用反引号转义。
 * - 保留大小写。
 * - 去除括起的双引号。
 */
interface CwtConfigPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()
    fun normalize(): CwtConfigPath = this

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): CwtConfigPath
        fun resolve(input: String): CwtConfigPath
        fun resolve(input: List<String>): CwtConfigPath
    }

    companion object : Resolver by CwtConfigPathResolverImpl()
}

@Suppress("unused")
fun CwtConfigPath.relativeTo(other: CwtConfigPath): CwtConfigPath? {
    if (this == other) return CwtConfigPath.resolveEmpty()
    if (this.isEmpty()) return other
    val subPaths = other.subPaths.removePrefixOrNull(this.subPaths) ?: return null
    return CwtConfigPath.resolve(subPaths)
}

package icu.windea.pls.model.paths

import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.model.paths.impl.CwtConfigPathResolverImpl

/**
 * 规则在规则文件中的路径。
 *
 * 说明：
 * - 相对于文件。
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

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): CwtConfigPath
        fun resolve(path: String): CwtConfigPath
        fun resolve(subPaths: List<String>): CwtConfigPath
    }

    companion object : Resolver by CwtConfigPathResolverImpl()
}

@Suppress("unused")
fun CwtConfigPath.relativeTo(other: CwtConfigPath): CwtConfigPath? {
    if (this == other) return CwtConfigPath.resolveEmpty()
    if (this.isEmpty()) return other
    val path = other.path.removePrefixOrNull(this.path + "/") ?: return null
    return CwtConfigPath.resolve(path)
}

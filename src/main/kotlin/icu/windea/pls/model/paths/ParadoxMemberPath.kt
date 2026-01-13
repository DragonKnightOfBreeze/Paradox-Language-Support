package icu.windea.pls.model.paths

import icu.windea.pls.core.collections.removePrefixOrNull
import icu.windea.pls.model.paths.impl.ParadoxMemberPathResolverImpl

/**
 * 脚本成员在脚本文件中的路径。
 *
 * 说明：
 * - 相对于脚本文件、定义或其他脚本成员。
 * - 如果对应深度的脚本成员是属性，则对应的子路径需要匹配其键。如果是单独的值，则需要是 `-`。
 * - 使用 "/" 分隔子路径。如果子路径中存在 "/"，会先用反引号转义。
 * - 保留大小写。
 * - 去除括起的双引号。
 *
 * 示例：
 * - （空字符串） - 对应所属脚本文件、定义或脚本成员本身。
 * - `foo` - 对应所属脚本文件、定义或脚本成员中，名为 `foo` 的属性。
 * - `foo/bar` - 对应所属脚本文件、定义或脚本成员中，名为 `foo` 的属性的值（块/子句）中，名为 `bar` 的属性。
 * - `foo/-` - 对应所属脚本文件、定义或脚本成员，名为 `foo` 的属性的值（块/子句）中，任意的值。
 */
interface ParadoxMemberPath : Iterable<String> {
    val path: String
    val subPaths: List<String> // 子路径中不用保留括起的双引号
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()
    fun normalize(): ParadoxMemberPath = this

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): ParadoxMemberPath
        fun resolve(input: String): ParadoxMemberPath
        fun resolve(input: List<String>): ParadoxMemberPath
    }

    companion object : Resolver by ParadoxMemberPathResolverImpl()
}

fun ParadoxMemberPath.relativeTo(other: ParadoxMemberPath): ParadoxMemberPath? {
    if (this == other) return ParadoxMemberPath.resolveEmpty()
    if (this.isEmpty()) return other
    val subPaths = other.subPaths.removePrefixOrNull(this.subPaths) ?: return null
    return ParadoxMemberPath.resolve(subPaths)
}

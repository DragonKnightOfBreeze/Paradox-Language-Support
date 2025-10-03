package icu.windea.pls.model.paths

import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.model.paths.impl.ParadoxElementPathResolverImpl

/**
 * 脚本成员在脚本文件中的路径。
 *
 * 说明：
 * - 相对于脚本文件，或者其他脚本成员。
 * - 使用 "/" 分隔子路径。如果子路径中存在 "/"，会先用反引号转义。
 * - 保留大小写。
 * - 去除括起的双引号。
 *
 * 可以用来表示：
 * - 脚本成员相对于所在文件的路径。
 * - 定义相对于所在文件的路径。
 * - 定义成员相对于所属定义的路径。
 *
 * 示例：
 * - （空字符串） - 对应所属文件或定义本身。
 * - `foo` - 对应所属文件或定义中名为 `foo` 的属性。
 * - `foo/bar` - 对应所属文件或定义中名为 `foo` 的属性的值（代码块）中，名为 `bar` 的属性。
 * - `foo/"bar"` - 对应所属文件或定义中名为 `foo` 的属性的值（代码块）中，名为 `bar`的属性（属性名在脚本中用引号括起）。
 * - `foo/-` - 对应所属文件或定义中名为 `foo` 的属性的值（代码块）中，任意的值。
 */
interface ParadoxElementPath : Iterable<String> {
    val path: String
    val subPaths: List<String> // 子路径中不用保留括起的双引号
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0

    override fun iterator(): Iterator<String> = this.subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): ParadoxElementPath
        fun resolve(path: String): ParadoxElementPath
        fun resolve(subPaths: List<String>): ParadoxElementPath
    }

    companion object: Resolver by ParadoxElementPathResolverImpl()
}

fun ParadoxElementPath.relativeTo(other: ParadoxElementPath): ParadoxElementPath? {
    if (this == other) return ParadoxElementPath.resolveEmpty()
    if (this.isEmpty()) return other
    val path = other.path.removePrefixOrNull(this.path + "/") ?: return null
    return ParadoxElementPath.resolve(path)
}

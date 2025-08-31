package icu.windea.pls.model.paths

import icu.windea.pls.model.paths.impl.ParadoxExpressionPathResolverImpl

/**
 * 表达式路径。保留大小写。
 *
 * 可以用来表示：
 * * 定义成员相对于所属定义的路径。
 * * 定义相对于所在文件的路径。
 *
 * 示例：
 * * （空字符串） - 对应所属文件或定义本身。
 * * `foo` - 对应所属文件或定义中名为"foo"的属性。
 * * `foo/bar` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性
 * * `foo/"bar"` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，名为"bar"的属性（属性名在脚本中用引号括起）
 * * `foo/-` - 对应所属文件或定义中名为"foo"的属性的值（代码块）中，任意的值
 *
 * @property path 使用"/"分隔的路径（预先移除括起的双引号）。
 * @property originalPath 使用"/"分隔的路径（保留括起的双引号）。
 */
interface ParadoxExpressionPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val originalPath: String
    val originalSubPaths: List<String>
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0

    override fun iterator(): Iterator<String> = this.subPaths.iterator()

    interface Resolver {
        fun resolveEmpty(): ParadoxExpressionPath
        fun resolve(originalPath: String): ParadoxExpressionPath
        fun resolve(originalSubPaths: List<String>): ParadoxExpressionPath
    }

    companion object: Resolver by ParadoxExpressionPathResolverImpl()
}

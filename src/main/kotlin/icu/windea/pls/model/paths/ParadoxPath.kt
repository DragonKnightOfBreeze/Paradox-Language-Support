package icu.windea.pls.model.paths

import icu.windea.pls.model.paths.impl.ParadoxPathResolverImpl

/**
 * 文件路径。相对于游戏或模组入口目录（不一定是根目录）。保留大小写。
 *
 * 示例：
 * - `common/buildings/00_capital_buildings.txt`
 * - `localisation/simp_chinese/l_simp_chinese.yml`
 *
 * @property path 使用"/"分割的路径。
 */
interface ParadoxPath : Iterable<String> {
    val path: String
    val subPaths: List<String>
    val parent: String
    val root: String
    val fileName: String
    val fileExtension: String?
    val length: Int

    fun isEmpty(): Boolean = length == 0
    fun isNotEmpty(): Boolean = length != 0
    fun get(index: Int): String = subPaths.getOrNull(index).orEmpty()

    override fun iterator(): Iterator<String> = subPaths.iterator()
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveEmpty(): ParadoxPath
        fun resolve(path: String): ParadoxPath
        fun resolve(subPaths: List<String>): ParadoxPath
    }

    companion object : Resolver by ParadoxPathResolverImpl()
}

fun ParadoxPath.matches(matcher: ParadoxPathMatcher): Boolean {
    return matcher.matches(this)
}

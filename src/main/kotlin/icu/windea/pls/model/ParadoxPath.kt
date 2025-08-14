@file:Suppress("unused")

package icu.windea.pls.model

import icu.windea.pls.core.*

/**
 * 文件路径，相对于游戏或模组目录，或者入口目录。保留大小写。
 *
 * 示例：
 * * `common/buildings/00_capital_buildings.txt`
 * * `localisation/simp_chinese/l_simp_chinese.yml`
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

    companion object Resolver {
        val Empty: ParadoxPath = EmptyParadoxPath

        fun resolve(path: String): ParadoxPath = doResolve(path)

        fun resolve(subPaths: List<String>): ParadoxPath = doResolve(subPaths)
    }
}

//Implementations (not interned)

private fun doResolve(path: String): ParadoxPath {
    if (path.isEmpty()) return EmptyParadoxPath
    return ParadoxPathImpl1(path)
}

private fun doResolve(subPaths: List<String>): ParadoxPath {
    if (subPaths.isEmpty()) return EmptyParadoxPath
    return ParadoxPathImpl2(subPaths)
}

private abstract class ParadoxPathImpl : ParadoxPath {
    override val root: String get() = subPaths.firstOrNull().orEmpty()
    override val fileName: String get() = subPaths.lastOrNull().orEmpty()
    override val fileExtension: String? get() = fileName.substringAfterLast('.', "").orNull()
    override val length: Int get() = subPaths.size

    override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

private class ParadoxPathImpl1(path: String) : ParadoxPathImpl() {
    override val path: String = path
    override val subPaths: List<String> = path.split('/')
    override val parent: String = path.substringBeforeLast('/', "")
}

private class ParadoxPathImpl2(subPaths: List<String>) : ParadoxPathImpl() {
    override val path: String = subPaths.joinToString("/")
    override val subPaths: List<String> = subPaths
    override val parent: String = path.substringBeforeLast('/', "")
}

private object EmptyParadoxPath : ParadoxPath {
    override val subPaths: List<String> = emptyList()
    override val path: String = ""
    override val parent: String = ""
    override val root: String = ""
    override val fileName: String = ""
    override val fileExtension: String? = null
    override val length: Int = 0

    override fun equals(other: Any?) = this === other || other is ParadoxPath && path == other.path
    override fun hashCode() = path.hashCode()
    override fun toString() = path
}

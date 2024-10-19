package icu.windea.pls.lang.search.scope

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

@Suppress("EqualsOrHashCode")
class ParadoxModAndGameSearchScope(
    project: Project,
    val contextFile: VirtualFile?,
    val modDirectory: VirtualFile?,
    val gameDirectory: VirtualFile?
) : ParadoxSearchScope(project) {
    @Suppress("DialogTitleCapitalization")
    override fun getDisplayName(): String {
        return PlsBundle.message("search.scope.name.modAndGame")
    }

    override fun contains(file: VirtualFile): Boolean {
        val contextFile0 = file.findTopHostFileOrThis()
        if (!ParadoxFileManager.canReference(contextFile, contextFile0)) return false //判断上下文文件能否引用另一个文件中的内容
        return (modDirectory != null && VfsUtilCore.isAncestor(modDirectory, contextFile0, false))
            || (gameDirectory != null && VfsUtilCore.isAncestor(gameDirectory, contextFile0, false))
    }

    override fun compare(file1: VirtualFile, file2: VirtualFile): Int {
        val order1 = getOrder(file1)
        val order2 = getOrder(file2)
        return order2.compareTo(order1)
    }

    private fun getOrder(file: VirtualFile): Int {
        //-1 - 找不到rootFile
        //0 - 位于游戏目录下
        val rootFile = selectRootFile(file) ?: return -1
        if (rootFile == gameDirectory) return 0
        if (rootFile == modDirectory) return 1
        return -1
    }

    override fun calcHashCode(): Int {
        if (gameDirectory == null && modDirectory == null) return 0
        var result = modDirectory.hashCode()
        result = result * 31 + gameDirectory.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxModAndGameSearchScope
            && contextFile == other.contextFile
            && modDirectory == other.modDirectory
            && gameDirectory == other.gameDirectory
    }

    override fun toString(): String {
        return "Paradox mod and game directory scope: $modDirectory, $gameDirectory"
    }
}

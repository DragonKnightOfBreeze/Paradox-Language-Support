package icu.windea.pls.lang.analysis

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import kotlin.io.path.notExists

object ParadoxGameManager {
    fun getQuickGameDirectory(gameType: ParadoxGameType): String? {
        val path = PlsPathService.getSteamGamePath(gameType.steamId, gameType.title)
        if (path == null || path.notExists()) return null
        return path.toString()
    }

    fun validateGameDirectory(builder: ValidationInfoBuilder, gameType: ParadoxGameType, gameDirectory: String?): ValidationInfo? {
        // 验证游戏目录是否合法
        // - 路径合法
        // - 路径对应的目录存在
        // - 路径是游戏目录
        val gameDirectory0 = gameDirectory?.normalizePath()?.orNull() ?: return null
        val path = gameDirectory0.toPathOrNull()
        if (path == null) return builder.error(PlsBundle.message("gameDirectory.error.1"))
        val rootFile = path.toVirtualFile(refreshIfNeed = true)?.takeIf { it.exists() }
        if (rootFile == null) return builder.error(PlsBundle.message("gameDirectory.error.2"))
        val rootInfo = rootFile.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return builder.error(PlsBundle.message("gameDirectory.error.3", gameType.title))
        return null
    }

    fun getGameVersionFromGameDirectory(gameDirectory: String?): String? {
        val gameDirectory0 = gameDirectory?.normalizePath()?.orNull() ?: return null
        val rootFile = gameDirectory0.toVirtualFile(true)?.takeIf { it.exists() } ?: return null
        val rootInfo = rootFile.rootInfo
        if (rootInfo !is ParadoxRootInfo.Game) return null
        return rootInfo.version
    }

    /**
     * 比较游戏版本。
     *
     * - 使用由点号分割的整数组成的游戏版本号，如 `3.14`。
     * - 允许通配符，如 "3.14.*"。
     * - 允许后缀，如 `3.99.1 beta`。
     */
    fun compareGameVersion(version1: String, version2: String): Int {
        val s1 = version1.splitByBlank(limit = 2)
        val s2 = version2.splitByBlank(limit = 2)
        val r = compareGameVersionNumbers(s1.first(), s2.first())
        if (r != 0) return r
        return compareGameVersionSuffix(s1.getOrNull(1), s2.getOrNull(1))
    }

    private fun compareGameVersionNumbers(numbers1: String, numbers2: String): Int {
        val l1 = numbers1.split('.')
        val l2 = numbers2.split('.')
        val maxSize = Integer.max(l1.size, l2.size)
        for (i in 0 until maxSize) {
            val r = compareGameVersionNumber(l1.getOrNull(i), l2.getOrNull(i))
            if (r != 0) return r
        }
        return 0
    }

    private fun compareGameVersionNumber(number1: String?, number2: String?): Int {
        val s1 = number1.orEmpty()
        val s2 = number2.orEmpty()
        if (s1 == "*" || s2 == "*") return 0
        if (s1 == s2) return 0
        val n1 = s1.toIntOrNull()
        val n2 = s2.toIntOrNull()
        if (n1 == null || n2 == null) return s1.compareTo(s2)
        return n1.compareTo(n2)
    }

    private fun compareGameVersionSuffix(suffix1: String?, suffix2: String?): Int {
        val s1 = suffix1.orEmpty()
        val s2 = suffix2.orEmpty()
        return when {
            s1.isEmpty() && s2.isEmpty() -> 0
            s1.isEmpty() -> 1
            s2.isEmpty() -> -1
            else -> s1.compareTo(s2)
        }
    }
}

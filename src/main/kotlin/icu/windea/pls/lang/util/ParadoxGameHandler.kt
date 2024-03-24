package icu.windea.pls.lang.util

import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

object ParadoxGameHandler {
    fun validateGameDirectory(builder: ValidationInfoBuilder, gameType: ParadoxGameType, gameDirectory: String?): ValidationInfo? {
        //验证游戏目录是否合法
        //* 路径合法
        //* 路径对应的目录存在
        //* 路径是游戏目录（可以查找到对应的launcher-settings.json）
        val gameDirectory0 = gameDirectory?.normalizeAbsolutePath()?.orNull() ?: return null
        val path = gameDirectory0.toPathOrNull()
        if(path == null) return builder.error(PlsBundle.message("gameDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, false)?.takeIf { it.exists() }
        if(rootFile == null) return builder.error(PlsBundle.message("gameDirectory.error.2"))
        val rootInfo = rootFile.rootInfo
        if(rootInfo !is ParadoxGameRootInfo) return builder.error(PlsBundle.message("gameDirectory.error.3", gameType.title))
        return null
    }
    
    fun getQuickGameDirectory(gameType: ParadoxGameType): String? {
        val path = getSteamGamePath(gameType.steamId, gameType.title)
        if(path == null || path.toPathOrNull()?.takeIf { it.exists() } == null) return null
        return path
    }
    
    fun getGameVersionFromGameDirectory(gameDirectory: String?): String? {
        val gameDirectory0 = gameDirectory?.normalizeAbsolutePath()?.orNull() ?: return null
        val rootFile = gameDirectory0.toVirtualFile(false)?.takeIf { it.exists() } ?: return null
        val rootInfo = rootFile.rootInfo
        if(rootInfo !is ParadoxGameRootInfo) return null
        return rootInfo.launcherSettingsInfo.rawVersion
    }
}
package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

/**
 * 当默认游戏类型变更后，重新解析文件。
 */
class ParadoxRefreshOnDefaultGameTypeChangedListener : ParadoxDefaultGameTypeListener {
    override fun onChange(gameType: ParadoxGameType) {
        val modDirectories = mutableSetOf<String>()
        getProfilesSettings().modDescriptorSettings.values.forEach { settings ->
            if(settings.gameType == null) {
                //这里可能包含不在项目中（以及库中）的根目录
                val modDirectory = settings.modDirectory
                if(modDirectory != null) modDirectories.add(modDirectory)
            }
        }
        
        //重新解析文件
        runWriteAction { ParadoxCoreHandler.reparseFilesByRootFilePaths(modDirectories) }
        
        //此时不需要刷新内嵌提示
    }
}
package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

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
        runWriteAction { ParadoxCoreHandler.reparseFilesByRootFilePaths(modDirectories) }
        
        //检查是否也需要强制刷新inlayHints - 不需要
    }
}
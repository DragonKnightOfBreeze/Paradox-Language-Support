package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(project: Project, modSettings: ParadoxModSettingsState, oldGameType: ParadoxGameType) {
        val gameType = modSettings.gameType
        
        //更新rootInfo缓存的游戏类型
        modSettings.modPath?.let { modPath -> refreshGameType(modPath, gameType) }
        modSettings.modDependencies.forEach { it.modPath?.let { path -> refreshGameType(path, gameType) } }
        
        //重新解析文件
        runWriteAction {
            modSettings.modPath?.let { modPath -> reparseFiles(modPath) }
            modSettings.modDependencies.forEach { it.modPath?.let { modPath -> reparseFiles(modPath) } }
        }
    }
    
    private fun refreshGameType(modPath: String, gameType: ParadoxGameType) {
        val rootInfo = ParadoxRootInfo.values[modPath]
        if(rootInfo is ParadoxModRootInfo) {
            rootInfo.gameType = gameType
        }
    }
    
    private fun reparseFiles(modPath: String) {
        val path = modPath.toPathOrNull() ?: return
        val rootFile = VfsUtil.findFile(path, true) ?: return
        ParadoxCoreHandler.reparseFilesInRoot(rootFile)
    }
}
   


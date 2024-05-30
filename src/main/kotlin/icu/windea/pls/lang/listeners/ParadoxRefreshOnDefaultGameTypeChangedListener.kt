package icu.windea.pls.lang.listeners

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * 当默认游戏类型变更后，重新解析文件。
 */
class ParadoxRefreshOnDefaultGameTypeChangedListener : ParadoxDefaultGameTypeListener {
    override fun onChange(oldGameType: ParadoxGameType, gameType: ParadoxGameType) {
        val modDirectories = mutableSetOf<String>()
        getProfilesSettings().modDescriptorSettings.values.forEach { settings ->
            if(settings.gameType == null) {
                //这里可能包含不在项目中（以及库中）的根目录
                val modDirectory = settings.modDirectory
                if(modDirectory != null) modDirectories.add(modDirectory)
            }
        }
        
        //重新解析文件（IDE之后会自动请求重新索引）
        val files = ParadoxCoreHandler.findFilesByRootFilePaths(modDirectories)
        ParadoxCoreHandler.reparseFiles(files)
        
        //此时不需要刷新内嵌提示
    }
}
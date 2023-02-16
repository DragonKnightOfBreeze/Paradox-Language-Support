package icu.windea.pls.core.listeners

import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxRefreshOnDefaultGameTypeChangedListener : ParadoxDefaultGameTypeListener {
    override fun onChange(gameType: ParadoxGameType) {
        val modPaths = mutableListOf<String>()
        getAllModSettings().descriptorSettings.values.forEach { settings ->
            if(settings.gameType == null) {
                //TODO 这里可能包含不在项目中（以及库中）的根目录
                val modPath = settings.modPath
                if(modPath != null) modPaths.add(modPath)
            }
        }
        ParadoxCoreHandler.reparseFilesInRoot(modPaths)
    }
}
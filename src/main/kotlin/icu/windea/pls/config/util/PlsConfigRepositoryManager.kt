package icu.windea.pls.config.util

import com.intellij.openapi.ui.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

object PlsConfigRepositoryManager {
    fun getDefaultConfigRepositoryUrl(gameType: ParadoxGameType): String {
        return "https://github.com/DragonKnightOfBreeze/cwtools-${gameType.id}-config"
    }

    fun getDefaultConfigRepositoryDirectoryName(gameType: ParadoxGameType): String {
        return "cwtools-${gameType.id}-config"
    }

    fun validateConfigRepositoryUrl(builder: ValidationInfoBuilder, gameType: ParadoxGameType, get: String): ValidationInfo? {
        //规则仓库URL应当包含对应的游戏类型ID

        TODO("Not yet implemented") //TODO 1.4.2
    }

    fun isValidToSync(): Boolean {
        val settings = PlsFacade.getConfigSettings()
        val valid = settings.enableRemoteConfigGroups
            && settings.remoteConfigDirectory.isNotNullOrEmpty()
            && settings.configRepositoryUrls.values.any { it.isNotNullOrEmpty() }
        return valid
    }

    fun syncFromConfigRepositoryUrls() {
        //这里需要先验证是否真的需要刷新
        val valid = isValidToSync()
        if (!valid) return

        TODO("Not yet implemented") //TODO 1.4.2
    }
}

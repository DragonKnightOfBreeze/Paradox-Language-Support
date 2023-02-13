package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import com.intellij.util.messages.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

/**
 * 监听模组配置的游戏类型的更改。
 */
interface ParadoxModGameTypeListener {
    fun onChange(project: Project, modSettings: ParadoxModSettingsState, oldGameType: ParadoxGameType)
    
    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic.create("ParadoxModGameTypeListener", ParadoxModGameTypeListener::class.java)
    }
}


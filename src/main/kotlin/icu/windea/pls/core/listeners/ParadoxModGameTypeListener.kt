package icu.windea.pls.core.listeners

import com.intellij.util.messages.*
import icu.windea.pls.core.settings.*

/**
 * 监听模组配置的游戏类型的更改。
 */
interface ParadoxModGameTypeListener {
    fun onChange(modSettings: ParadoxModSettingsState)
    
    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxModGameTypeListener::class.java, Topic.BroadcastDirection.NONE)
    }
}


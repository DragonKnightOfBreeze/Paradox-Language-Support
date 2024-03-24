package icu.windea.pls.lang.listeners

import com.intellij.util.messages.*
import icu.windea.pls.core.settings.*

/**
 * 监听模组配置的更改。
 */
interface ParadoxModSettingsListener {
    fun onAdd(modSettings: ParadoxModSettingsState)
    
    fun onChange(modSettings: ParadoxModSettingsState)
    
    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxModSettingsListener::class.java, Topic.BroadcastDirection.NONE)
    }
}

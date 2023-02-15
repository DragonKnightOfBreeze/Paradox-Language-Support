package icu.windea.pls.core.listeners

import com.intellij.util.messages.*
import icu.windea.pls.core.settings.*

/**
 * 监听模组配置的更改。
 */
interface ParadoxModSettingsListener {
    fun onChange(modSettings: ParadoxModSettingsState)
    
    companion object {
        @Topic.AppLevel
        val TOPIC = Topic.create("ParadoxModSettingsListener", ParadoxModSettingsListener::class.java)
    }
}


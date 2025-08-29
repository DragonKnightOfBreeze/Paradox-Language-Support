package icu.windea.pls.lang.listeners

import com.intellij.util.messages.Topic
import icu.windea.pls.lang.settings.ParadoxGameSettingsState

/**
 * 监听游戏配置的更改。
 */
interface ParadoxGameSettingsListener {
    fun onAdd(gameSettings: ParadoxGameSettingsState)

    fun onChange(gameSettings: ParadoxGameSettingsState)

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(ParadoxGameSettingsListener::class.java, Topic.BroadcastDirection.NONE)
    }
}

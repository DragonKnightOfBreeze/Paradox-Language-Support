package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import com.intellij.util.messages.*
import icu.windea.pls.core.settings.*

/**
 * 对于单个模组，监听游戏根目录和模组依赖根目录的变化。
 */
interface ParadoxModSettingsListener {
    fun onChange(project: Project, modSettings: ParadoxModSettingsState)
    
    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic.create("ParadoxModSettingsListener", ParadoxModSettingsListener::class.java)
    }
}

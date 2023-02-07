package icu.windea.pls.core.settings

import com.intellij.openapi.project.*
import com.intellij.util.messages.*

/**
 * 对于单个模组，监听游戏根目录和模组依赖根目录的变化。
 */
interface ParadoxModSettingsChangedListener {
    fun onChanged(project: Project, modSettings: ParadoxModSettingsState)
    
    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic.create("ParadoxModSettingsChangedListener", ParadoxModSettingsChangedListener::class.java)
    }
}


package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import com.intellij.util.messages.*
import icu.windea.pls.core.settings.*

/**
 * 监听模组配置的更改。
 */
interface ParadoxModSettingsListener {
    fun onChange(project: Project, modSettings: ParadoxModSettingsState)
    
    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic.create("ParadoxModSettingsListener", ParadoxModSettingsListener::class.java)
    }
}


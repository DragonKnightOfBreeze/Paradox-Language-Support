package icu.windea.pls.config.listeners

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

/**
 * 监听规则分组的刷新状态的更改。
 */
interface CwtConfigGroupRefreshStatusListener {
    fun onChange(project: Project)

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic.create("CwtConfigGroupRefreshStatusListener", CwtConfigGroupRefreshStatusListener::class.java)
    }
}

package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

// com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectNotificationAware

/**
 * 监听规则分组的刷新状态的更改。
 */
interface CwtConfigGroupRefreshStatusListener {
    fun onChange(project: Project)

    companion object {
        @Topic.AppLevel
        val TOPIC = Topic(CwtConfigGroupRefreshStatusListener::class.java)
    }
}

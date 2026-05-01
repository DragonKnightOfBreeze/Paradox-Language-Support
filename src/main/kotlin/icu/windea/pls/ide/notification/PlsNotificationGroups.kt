package icu.windea.pls.ide.notification

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager

object PlsNotificationGroups {
    fun global(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls")
    }

    fun settings(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls.settings")
    }

    fun diff(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls.diff")
    }

    fun manipulation(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls.manipulation")
    }
}

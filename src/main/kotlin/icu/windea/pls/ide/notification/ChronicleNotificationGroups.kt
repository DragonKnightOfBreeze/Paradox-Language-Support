package icu.windea.pls.ide.notification

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager

object ChronicleNotificationGroups {
    fun global(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("chronicle")
    }

    fun settings(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("chronicle.settings")
    }

    fun diff(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("chronicle.diff")
    }

    fun manipulation(): NotificationGroup {
        return NotificationGroupManager.getInstance().getNotificationGroup("chronicle.manipulation")
    }
}

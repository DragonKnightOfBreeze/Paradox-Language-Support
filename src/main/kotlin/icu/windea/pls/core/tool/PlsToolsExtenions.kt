package icu.windea.pls.core.tool

import com.intellij.notification.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.settings.*

fun notify(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
    NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
        settings.qualifiedName,
        message,
        NotificationType.INFORMATION
    ).notify(project)
}

fun notifyWarning(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
    NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
        settings.qualifiedName,
        message,
        NotificationType.WARNING
    ).notify(project)
}
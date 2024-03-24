package icu.windea.pls.lang.tools

import com.intellij.notification.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.settings.*

fun notify(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
    val qualifiedName = settings.qualifiedName ?: return //should not be null
    NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
        qualifiedName,
        message,
        NotificationType.INFORMATION
    ).notify(project)
}

fun notifyWarning(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
    val qualifiedName = settings.qualifiedName ?: return //should not be null
    NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
        qualifiedName,
        message,
        NotificationType.WARNING
    ).notify(project)
}
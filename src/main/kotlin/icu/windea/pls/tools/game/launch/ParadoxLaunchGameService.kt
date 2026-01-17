package icu.windea.pls.tools.game.launch

import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.project.Project
import icu.windea.pls.model.ParadoxRootInfo

interface ParadoxLaunchGameService {
    fun launchGameInSteam(rootInfo: ParadoxRootInfo)

    fun showNotification(rootInfo: ParadoxRootInfo, project: Project)

    fun exitIde()

    companion object {
        @JvmStatic
        fun getInstance(): ParadoxLaunchGameService = serviceOrNull() ?: ParadoxLaunchGameServiceImpl()
    }
}

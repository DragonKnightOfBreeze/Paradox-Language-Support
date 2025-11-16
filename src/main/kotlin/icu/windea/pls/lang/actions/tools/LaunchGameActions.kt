package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.tools.ParadoxLaunchGameService

interface LaunchGameActions {
    open class InSteam : LaunchGameActionBase() {
        // - 兼容游戏未拥有或未安装的情况
        // - 在描述中显示具体的游戏标题
        // - 无法直接地监听到游戏的启动状态

        override fun update(e: AnActionEvent) {
            // 判断是否需要显示在编辑器工具栏中
            if (e.place == ActionPlaces.EDITOR_TOOLBAR) {
                val show = PlsFacade.getSettings().state.others.showLaunchGameActionInEditorContextToolbar
                e.presentation.isEnabledAndVisible = show
                if (!show) return
            }

            val rootInfo = getRootInfo(e)
            val isAvailable = rootInfo != null
            e.presentation.isEnabledAndVisible = isAvailable
            if (!isAvailable) return
            val gameType = rootInfo.gameType
            e.presentation.text = PlsBundle.message("game.launcher.inSteam")
            e.presentation.description = PlsBundle.message("game.launcher.inSteam.detail", gameType.title)
        }

        override fun actionPerformed(e: AnActionEvent) {
            val rootInfo = getRootInfo(e) ?: return
            ParadoxLaunchGameService.launchGameInSteam(rootInfo)
            val project = e.project ?: return
            ParadoxLaunchGameService.showNotification(rootInfo, project)
        }
    }

    class InSteamWithExit : LaunchGameActionBase() {
        // - 兼容游戏未拥有或未安装的情况
        // - 在描述中显示具体的游戏标题
        // - 无法直接地监听到游戏的启动状态
        // - 打开链接后直接退出 IDE（如果启用，则显示退出确认提示）
        // - 可能需要等待一段时间后才会真正启动游戏

        override fun update(e: AnActionEvent) {
            val rootInfo = getRootInfo(e)
            val isAvailable = rootInfo != null
            e.presentation.isEnabledAndVisible = isAvailable
            if (!isAvailable) return
            val gameType = rootInfo.gameType
            e.presentation.text = PlsBundle.message("game.launcher.inSteam.withExit")
            e.presentation.description = PlsBundle.message("game.launcher.inSteam.withExit.detail", gameType.title)
        }

        override fun actionPerformed(e: AnActionEvent) {
            val rootInfo = getRootInfo(e) ?: return
            ParadoxLaunchGameService.launchGameInSteam(rootInfo)
            ParadoxLaunchGameService.exitIde()
        }
    }
}

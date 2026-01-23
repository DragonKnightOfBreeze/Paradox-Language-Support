package icu.windea.pls

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.core.isClassPresent
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants
import kotlinx.coroutines.CoroutineScope

/**
 * 通用门面，用于获取协程作用域、各种服务以及各种插件设置。
 */
@Suppress("unused")
object PlsFacade {
    // from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.APP, Service.Level.PROJECT)
    private class CoroutineScopeService(val coroutineScope: CoroutineScope)

    fun getCoroutineScope(): CoroutineScope = service<CoroutineScopeService>().coroutineScope

    fun getCoroutineScope(project: Project): CoroutineScope = project.service<CoroutineScopeService>().coroutineScope

    /**
     * 得到默认项目的指定游戏类型的规则分组。不能用于访问 PSI。
     *
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance().getConfigGroup(finalGameType)
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     *
     * @param project 指定的项目。如果是默认项目，则不能用于访问 PSI。
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(project: Project, gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance(project).getConfigGroup(finalGameType)
    }

    /**
     * 检查指定项目与上下文（[context]）的规则分组是否已加载完毕。
     *
     * @param project 指定的项目。
     * @param context 用于获取游戏类型的上下文对象。
     */
    fun checkConfigGroupInitialized(project: Project, context: Any?): Boolean {
        return CwtConfigGroupService.getInstance(project).checkConfigGroupInitialized(context)
    }

    fun createNotification(notificationType: NotificationType, content: String): Notification {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls")
            .createNotification(content, notificationType)
    }

    fun createNotification(notificationType: NotificationType, title: String, content: String): Notification {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls")
            .createNotification(title, content, notificationType)
    }

    /** 是否正在进行单元测试。 */
    fun isUnitTestMode(): Boolean {
        return ApplicationManager.getApplication().let { it == null || it.isUnitTestMode }
    }

    /** 是否正在调试。 */
    fun isDebug(): Boolean {
        return System.getProperty("pls.is.debug").toBoolean()
    }

    /** 是否是开发中版本。 */
    fun isDevVersion(): Boolean {
        return PluginManagerCore.getPlugin(PlsConstants.pluginId)?.version?.endsWith("-dev") == true
    }

    /**
     * 用于检查插件的各种可选择的能力。
     */
    object Capacities {
        /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。 */
        fun includeSqlite() = "org.sqlite.JDBC".isClassPresent()

        /** 是否记录缓存状态。 */
        fun recordCacheStats() = System.getProperty("pls.record.cache.status").toBoolean()

        /** 是否启用更宽松的优化策略。这适用于多数需要加入到缓存中的集合，进行更详细的忽略检查。 */
        fun relaxOptimize() = System.getProperty("pls.relax.optimize").toBoolean()

        /** 是否在打开项目后，刷新内置规则目录（仅限一次）。 */
        fun refreshBuiltIn() = System.getProperty("pls.refresh.builtIn").toBoolean()
    }
}

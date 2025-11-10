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
import icu.windea.pls.config.settings.PlsConfigSettings
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.isClassPresent
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxGameSettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.settings.PlsProfilesSettings
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constants.PlsConstants
import kotlinx.coroutines.CoroutineScope

/**
 * 通用门面，用于获取协程作用域、各种服务以及各种插件设置。
 */
object PlsFacade {
    // from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.APP, Service.Level.PROJECT)
    private class CoroutineScopeService(val coroutineScope: CoroutineScope)

    fun getCoroutineScope() = service<CoroutineScopeService>().coroutineScope

    fun getCoroutineScope(project: Project) = project.service<CoroutineScopeService>().coroutineScope

    /**
     * 得到默认项目的指定游戏类型的规则分组。不能用来访问 PSI。
     *
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return service<CwtConfigGroupService>().getConfigGroup(getDefaultProject(), finalGameType)
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     *
     * @param project 指定的项目。如果是默认项目，则不能用来访问 PSI。
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(project: Project, gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return service<CwtConfigGroupService>().getConfigGroup(project, finalGameType)
    }

    fun getSettings() = service<PlsSettings>().state

    fun getConfigSettings() = service<PlsConfigSettings>().state

    fun getIntegrationsSettings() = service<PlsIntegrationsSettings>().state

    fun getProfilesSettings() = service<PlsProfilesSettings>().state

    fun getGameSettings(rootInfo: ParadoxRootInfo.Game): ParadoxGameSettingsState? {
        return getProfilesSettings().gameSettings.get(rootInfo.rootFile.path)
    }

    fun getModSettings(rootInfo: ParadoxRootInfo.Mod): ParadoxModSettingsState? {
        return getProfilesSettings().modSettings.get(rootInfo.rootFile.path)
    }

    fun getGameOrModSettings(rootInfo: ParadoxRootInfo): ParadoxGameOrModSettingsState? {
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> getGameSettings(rootInfo)
            is ParadoxRootInfo.Mod -> getModSettings(rootInfo)
            else -> null
        }
    }

    fun getInternalSettings() = service<PlsInternalSettings>()

    fun createNotification(notificationType: NotificationType, content: String): Notification {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls")
            .createNotification(content, notificationType)
    }

    fun createNotification(notificationType: NotificationType, title: String, content: String): Notification {
        return NotificationGroupManager.getInstance().getNotificationGroup("pls")
            .createNotification(title, content, notificationType)
    }

    /** 是否正在进行单元测试。*/
    fun isUnitTestMode(): Boolean {
        return ApplicationManager.getApplication().let { it == null || it.isUnitTestMode }
    }

    /** 是否正在调试。*/
    fun isDebug(): Boolean {
        return System.getProperty("pls.is.debug").toBoolean()
    }

    /** 是否是开发中版本。*/
    fun isDevVersion(): Boolean {
        return PluginManagerCore.getPlugin(PlsConstants.pluginId)?.version?.endsWith("-dev") == true
    }

    /**
     * 用于检查插件的各种可选择的能力。
     */
    object Capacities {
        /** 是否包含 SQLite 驱动包，从而启用与 SQLite 相关的各种功能。*/
        fun includeSqlite() = "org.sqlite.JDBC".isClassPresent()

        /** 是否记录缓存状态。*/
        fun recordCacheStats() = System.getProperty("pls.cache.recordStats").toBoolean()

        /** 是否启用更严格的优化策略。这适用于多数需要加入到缓存中的集合，进行更准确的忽略检查。 */
        fun strictOptimize() = System.getProperty("pls.strict.optimize").toBoolean()

        /** 是否禁用在打开项目后，刷新内置规则目录（仅限一次）。 */
        fun suppressRefreshBuiltIn() = System.getProperty("pls.suppress.refreshBuiltIn").toBoolean()
    }
}

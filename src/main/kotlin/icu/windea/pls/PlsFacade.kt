package icu.windea.pls

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.config.settings.PlsConfigSettings
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.PlsDataProvider
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
 * 用于获取协程作用域、各种服务以及各种插件设置。
 */
object PlsFacade {
    //from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.PROJECT)
    private class ProjectService(val coroutineScope: CoroutineScope)

    @Service(Service.Level.APP)
    private class ApplicationService(val coroutineScope: CoroutineScope)

    fun getCoroutineScope(project: Project) = project.service<ProjectService>().coroutineScope

    fun getCoroutineScope() = service<ApplicationService>().coroutineScope

    fun getDataProvider() = service<PlsDataProvider>()

    fun getConfigGroupService() = service<CwtConfigGroupService>()

    /**
     * 得到默认项目的指定游戏类型的规则分组。不能用来访问 PSI。
     *
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return getConfigGroupService().getConfigGroup(getDefaultProject(), finalGameType)
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     *
     * @param project 指定的项目。如果是默认项目，则不能用来访问 PSI。
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(project: Project, gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return getConfigGroupService().getConfigGroup(project, finalGameType)
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

    /**
     * 是否正在进行单元测试。
     */
    fun isUnitTestMode(): Boolean {
        return ApplicationManager.getApplication().let { it == null || it.isUnitTestMode }
    }

    /**
     * 是否正在调试。
     */
    fun isDebug(): Boolean {
        return System.getProperty("pls.is.debug").toBoolean()
    }

    /**
     * 是否是开发中版本。
     */
    fun isDevVersion(): Boolean {
        return PluginManagerCore.getPlugin(PluginId.findId(PlsConstants.pluginId))?.version?.endsWith("-dev") == true
    }
}

package icu.windea.pls

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
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
import kotlinx.coroutines.CoroutineScope

/**
 * 用于获取协程作用域、各种服务以及插件设置状态。
 */
object PlsFacade {
    val isDebug = System.getProperty("pls.is.debug").toBoolean()

    //from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.PROJECT)
    private class ProjectService(val coroutineScope: CoroutineScope)

    @Service(Service.Level.APP)
    private class ApplicationService(val coroutineScope: CoroutineScope)

    fun getCoroutineScope(project: Project) = project.service<ProjectService>().coroutineScope

    fun getCoroutineScope() = service<ApplicationService>().coroutineScope

    fun getDataProvider() = service<PlsDataProvider>()

    fun getConfigGroup(gameType: ParadoxGameType?) = getDefaultProject().service<CwtConfigGroupService>().getConfigGroup(gameType)

    fun getConfigGroup(project: Project, gameType: ParadoxGameType?) = project.service<CwtConfigGroupService>().getConfigGroup(gameType)

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
}

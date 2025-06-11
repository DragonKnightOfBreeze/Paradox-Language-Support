package icu.windea.pls

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.settings.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*

/**
 * 用于获取协程作用域、各种服务以及插件设置状态。
 */
object PlsFacade {
    //from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.PROJECT)
    private class ProjectService(val project: Project, val coroutineScope: CoroutineScope)

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
}


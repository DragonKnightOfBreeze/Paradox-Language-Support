package icu.windea.pls.lang

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.application
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.config.configGroupLibrary
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.images.ImageManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.constants.PlsPathConstants
import kotlinx.coroutines.launch

/**
 * 用于在特定生命周期执行特定的代码，例如，在IDE启动时初始化一些缓存数据。
 */
class PlsLifecycleListener : AppLifecycleListener, DynamicPluginListener, ProjectActivity {
    // for whole application

    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        ImageManager.registerImageIOSpi()
        // init caches for specific services and initializers
        initCaches()
    }

    private fun initCaches() {
        if (application.isUnitTestMode) return

        PlsPathConstants.initAsync()
        service<PlsDataProvider>().initAsync()
        getDefaultProject().service<CwtConfigGroupService>().initAsync()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId.idString == PlsConstants.pluginId) {
            ImageManager.registerImageIOSpi()
        }
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId.idString == PlsConstants.pluginId) {
            ImageManager.deregisterImageIOSpi()
        }
    }

    // for each project

    override suspend fun execute(project: Project) {
        // init caches for specific services and initializers
        initCaches(project)
        // refresh roots for libraries on project startup
        refreshRootsForLibraries(project)
        // refresh opened files on project startup (once only)
        refreshOnlyForOpenedFiles(project)
    }

    private fun initCaches(project: Project) {
        if (application.isUnitTestMode) return

        project.service<CwtConfigGroupService>().initAsync()
    }

    private fun refreshRootsForLibraries(project: Project) {
        if (application.isUnitTestMode) return

        project.paradoxLibrary.refreshRoots()
        project.configGroupLibrary.refreshRoots()
    }

    private val refreshedProjectIdsKey = createKey<MutableSet<String>>("pls.refreshedProjectIds")
    private val refreshedProjectIds by lazy { application.getOrPutUserData(refreshedProjectIdsKey) { mutableSetOf() } }

    private fun refreshOnlyForOpenedFiles(project: Project) {
        if (application.isUnitTestMode) return

        // 在IDE启动后首次打开某个项目时，刷新此项目已打开的脚本文件和本地化文件
        // 否则，如果插件更新后内置的规则文件也更新了，对于已打开的脚本文件和文本化文件，
        // 缓存的代码检查结果、内嵌提示等信息可能未正确刷新，仍然是过时的，需要通过文件更改来触发刷新

        if (!PlsFacade.getInternalSettings().refreshOnProjectStartup) return
        if (!refreshedProjectIds.add(project.locationHash)) return

        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        if (openedFiles.isEmpty()) return

        // 需要确保此项目的所有规则分组的数据已加载完毕
        // 仅重启高亮可能不足以让依赖规则的解析/缓存失效，强制重解析可确保首次打开即使用最新规则
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            val configGroupService = project.service<CwtConfigGroupService>()
            configGroupService.getConfigGroup(null).init()
            ParadoxGameType.entries.forEach { gameType -> configGroupService.getConfigGroup(gameType).init() }

            PlsCoreManager.reparseFiles(openedFiles)
        }
    }
}

package icu.windea.pls.lang

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.config.configGroupLibrary
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.withDoubleLock
import icu.windea.pls.images.ImageManager
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.lang.util.PlsAnalyzeManager
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.constants.PlsPathConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 用于在特定生命周期执行特定的代码，例如，在IDE启动时初始化一些缓存数据。
 */
class PlsLifecycleListener : AppLifecycleListener, DynamicPluginListener, ProjectActivity {
    private val runOncePerApplication = AtomicBoolean(false)
    private val mutex = Mutex()

    // for whole application

    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        ImageManager.registerImageIOSpi()
        // 在启动应用后，异步地初始化缓存数据
        initCachesAsync()
        // 在启动应用后，异步地预加载默认项目的规则数据（诸如设置页面等地方会用到）
        initDefaultConfigGroupsAsync()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId == PlsConstants.pluginId) {
            ImageManager.registerImageIOSpi()
        }
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId == PlsConstants.pluginId) {
            ImageManager.deregisterImageIOSpi()
        }
    }

    // for each project

    override suspend fun execute(project: Project) {
        // 这些操作仅需执行一次（应用范围）
        mutex.withDoubleLock(runOncePerApplication) {
            // 在打开项目后，刷新内置规则目录，从而确保能读取到最新的内置规则文件
            refreshBuiltInConfigRootDirectoriesAsync(project)
        }

        // 在打开项目后，异步地预加载规则数据
        initConfigGroupsAsync(project)
    }

    private fun initCachesAsync() {
        PlsPathConstants.initAsync()
        PlsPathService.initAsync()
    }

    @Suppress("ObsoleteDispatchersEdt")
    private suspend fun refreshBuiltInConfigRootDirectoriesAsync(project: Project) {
        if (PlsFacade.isUnitTestMode()) return // 单元测试时不自动刷新内置规则目录
        if (!PlsFacade.Capacities.refreshBuiltIn()) return // 必须显式启用

        val files = CwtConfigManager.getBuiltInConfigRootDirectories(project)
        if (files.isEmpty()) return

        // 必须先切换到 EDT
        withContext(Dispatchers.EDT) {
            // 显示可以取消的模态进度条
            val title = PlsBundle.message("configGroup.refresh.builtin.progressTitle")
            runWithModalProgressBlocking(project, title) {
                files.forEach { VfsUtil.markDirtyAndRefresh(false, true, true, it) }
            }
        }
    }

    private fun initDefaultConfigGroupsAsync() {
        if (PlsFacade.isUnitTestMode()) return // 单元测试时不自动加载规则数据
        CwtConfigGroupService.getInstance().initAsync()
    }

    private fun initConfigGroupsAsync(project: Project) {
        if (PlsFacade.isUnitTestMode()) return // 单元测试时不自动加载规则数据
        if (project.isDisposed) return
        CwtConfigGroupService.getInstance(project).initAsync {
            // 重新解析已打开的文件
            val openedFiles = PlsAnalyzeManager.findOpenedFiles(onlyParadoxFiles = true)
            PlsAnalyzeManager.reparseFiles(openedFiles)
            // 规则数据加载完毕后，异步地刷新外部库的根目录
            refreshRootsForLibrariesAsync(project)
        }
    }

    private fun refreshRootsForLibrariesAsync(project: Project) {
        if (project.isDefault) return
        if (project.isDisposed) return
        project.configGroupLibrary.refreshRootsAsync()
        project.paradoxLibrary.refreshRootsAsync()
    }
}

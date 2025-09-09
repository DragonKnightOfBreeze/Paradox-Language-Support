package icu.windea.pls.lang

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.application
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroupLibrary
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.withDoubleLock
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.images.ImageManager
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
        // 在应用启动时，异步地初始化缓存数据
        initCachesAsync()
        // 在应用启动时，异步地预加载默认项目的规则数据（诸如设置页面等地方会用到）
        initConfigGroupsAsync(getDefaultProject())
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
        // 这些操作仅需执行一次（应用范围）
        mutex.withDoubleLock(runOncePerApplication) {
            // 仅限一次，如果必要，刷新内置规则目录
            refreshBuiltInConfigRootDirectoriesAsync(project)
        }

        // 在项目启动时，异步地预加载规则数据
        initConfigGroupsAsync(project)
        // 在项目启动时，异步地刷新外部库的根目录
        refreshRootsForLibrariesAsync(project)
    }

    private fun initCachesAsync() {
        if (application.isUnitTestMode) return
        PlsPathConstants.initAsync()
        PlsFacade.getDataProvider().initAsync()
    }

    @Suppress("ObsoleteDispatchersEdt")
    private suspend fun refreshBuiltInConfigRootDirectoriesAsync(project: Project) {
        if (application.isUnitTestMode) return
        // 确保能读取到最新的内置规则文件（仅限开发中版本，或者调试环境）
        if (!PlsFacade.isDebug() && !PlsFacade.isDevVersion()) return
        val builtInConfigRootDirectories = CwtConfigGroupFileProvider.EP_NAME.extensionList
            .filter { it.type == CwtConfigGroupFileProvider.Type.BuiltIn }
            .mapNotNull { it.getRootDirectory(project) }
        if (builtInConfigRootDirectories.isEmpty()) return

        // 必须先切换到 EDT
        withContext(Dispatchers.EDT) {
            // 显示可以取消的模态进度条
            val title = PlsBundle.message("configGroup.refresh.builtin.progressTitle")
            runWithModalProgressBlocking(project, title) {
                builtInConfigRootDirectories.forEach {
                    VfsUtil.markDirtyAndRefresh(false, true, true, it)
                }
            }
        }
    }

    private fun refreshRootsForLibrariesAsync(project: Project) {
        if (application.isUnitTestMode) return
        if (project.isDisposed) return
        project.paradoxLibrary.refreshRootsAsync()
        project.configGroupLibrary.refreshRootsAsync()
    }

    private fun initConfigGroupsAsync(project: Project) {
        if (application.isUnitTestMode) return
        if (project.isDisposed) return
        PlsFacade.getConfigGroupService().initAsync(project)
    }
}

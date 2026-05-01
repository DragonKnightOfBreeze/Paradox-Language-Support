package icu.windea.pls.ide

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import icu.windea.pls.PlsCapacities
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.core.withDoubleLock
import icu.windea.pls.lang.tools.PlsPathService
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 用于在特定生命周期执行特定的代码，例如，在 IDE 启动时初始化一些缓存数据。
 */
class PlsLifecycleListener : AppLifecycleListener, ProjectActivity {
    private val runOncePerApplication = AtomicBoolean(false)
    private val mutex = Mutex()

    // for whole application

    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        // 在启动应用后，异步地初始化缓存数据
        initCachesAsync()
        // 在启动应用后，异步地预加载应用级别的规则数据（诸如设置页面等地方会用到）
        initApplicationConfigGroupsAsync()
    }

    // for each project

    override suspend fun execute(project: Project) {
        // 这些操作仅需执行一次（应用范围）
        mutex.withDoubleLock(runOncePerApplication) {
            // 在打开项目后，刷新内置规则目录，从而确保能读取到最新的内置规则文件
            refreshBuiltInConfigFiles(project)
        }

        // 在打开项目后，异步地预加载规则数据
        initConfigGroupsAsync(project)
    }

    private fun initCachesAsync() {
        PlsPathService.getInstance().initAsync()
    }

    private fun initApplicationConfigGroupsAsync() {
        if (PlsFacade.isUnitTestMode()) return // 单元测试时不自动加载规则数据
        CwtConfigGroupService.getInstance().initConfigGroupsAsync()
    }

    private suspend fun refreshBuiltInConfigFiles(project: Project) {
        if (project.isDefault || project.isDisposed) return
        if (PlsFacade.isUnitTestMode()) return // 单元测试时不自动刷新内置规则目录
        if (!PlsCapacities.refreshBuiltIn()) return // 必须显式启用
        CwtConfigGroupService.getInstance().refreshBuiltInConfigFiles(project)
    }

    private fun initConfigGroupsAsync(project: Project) {
        if (project.isDefault || project.isDisposed) return
        if (PlsFacade.isUnitTestMode()) return // 单元测试时不自动加载规则数据
        CwtConfigGroupService.getInstance(project).initConfigGroupsAsync()
    }
}

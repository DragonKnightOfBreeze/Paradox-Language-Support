package icu.windea.pls

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ChronicleConstants
import kotlinx.coroutines.CoroutineScope

@Suppress("unused")
object ChronicleFacade {
    // from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.APP, Service.Level.PROJECT)
    private class CoroutineScopeService(val coroutineScope: CoroutineScope)

    /**
     * 得到应用级别的协程作用域。
     */
    fun getCoroutineScope(): CoroutineScope {
        return service<CoroutineScopeService>().coroutineScope
    }

    /**
     * 得到指定项目的协程作用域。
     */
    fun getCoroutineScope(project: Project): CoroutineScope {
        return project.service<CoroutineScopeService>().coroutineScope
    }

    /**
     * 得到应用级别的指定游戏类型的规则分组（不能用于访问 PSI）。
     *
     * 如果获取得到的游戏类型是 `null` 或 [ParadoxGameType.Core]，则会得到通用的规则分组。
     *
     * @param context 用于获取游戏类型的上下文对象（参见 [selectGameType]）。
     */
    fun getConfigGroup(context: Any? = null): CwtConfigGroup {
        val gameType = selectGameType(context) ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance().getConfigGroup(gameType)
    }

    /**
     * 得到指定项目与游戏类型的规则分组。
     *
     * 如果指定的项目是默认项目，则会得到应用级别的规则分组（不能用于访问 PSI）。
     * 如果获取得到的游戏类型是 `null` 或 [ParadoxGameType.Core]，则会得到通用的规则分组。
     *
     * @param project 指定的项目。
     * @param context 用于获取游戏类型的上下文对象（参见 [selectGameType]）。
     */
    fun getConfigGroup(project: Project, context: Any? = null): CwtConfigGroup {
        val gameType = selectGameType(context) ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance(project).getConfigGroup(gameType)
    }

    /**
     * 得到应用级别的所有规则分组（不能用于访问 PSI）。
     */
    fun getConfigGroups(): Map<ParadoxGameType, CwtConfigGroup> {
        return CwtConfigGroupService.getInstance().getConfigGroups()
    }

    /**
     * 得到指定项目的所有规则分组。
     *
     * 如果指定的项目是默认项目，则会得到应用级别的规则分组（不能用于访问 PSI）。
     *
     * @param project 指定的项目。
     */
    fun getConfigGroups(project: Project): Map<ParadoxGameType, CwtConfigGroup> {
        return CwtConfigGroupService.getInstance(project).getConfigGroups()
    }

    /**
     * 检查指定项目与上下文的规则分组是否已加载完毕。
     *
     * 如果指定的项目是默认项目，则会检查应用级别的规则分组。
     * 如果获取得到的游戏类型是 `null` 或 [ParadoxGameType.Core]，则会检查通用的规则分组。
     *
     * @param project 指定的项目。
     * @param context 用于获取游戏类型的上下文对象（参见 [selectGameType]）。
     */
    fun checkConfigGroupInitialized(project: Project, context: Any? = null): Boolean {
        val gameType = selectGameType(context) ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance(project).checkConfigGroupInitialized(gameType)
    }

    /**
     * 从 [event] 得到对应的规则分组。
     */
    fun getConfigGroup(event: AnActionEvent): CwtConfigGroup {
        val project = event.project
        val finalGameType = selectGameType(event.getData(CommonDataKeys.VIRTUAL_FILE)) ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance(project).getConfigGroup(finalGameType)
    }

    /**
     * 检查 IDE 是否正处于单元测试模式。或者，是否正在进行不依赖于平台的单元测试。
     */
    fun isUnitTestMode(): Boolean {
        return ApplicationManager.getApplication().let { it == null || it.isUnitTestMode }
    }

    /**
     * 检查 IDE 是否正处于内部模式。
     */
    fun isInternal(): Boolean {
        return ApplicationManager.getApplication().let { it != null && it.isInternal }
    }

    /**
     * 检查插件是否是开发中版本。
     */
    fun isDevVersion(): Boolean {
        // NOTE 3.0.0 [compatibility] `PluginManager.findEnabledPlugin(PluginId)` is internal (but ignored) since IDEA-262
        //  - Use `PluginDetailsService` instead
        return PluginManager.getInstance().findEnabledPlugin(ChronicleConstants.pluginId)?.version?.endsWith("-dev") == true
    }
}

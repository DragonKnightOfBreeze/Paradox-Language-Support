package icu.windea.pls

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
import kotlinx.coroutines.CoroutineScope

@Suppress("unused")
object PlsFacade {
    // from official documentation: Never acquire service instances prematurely or store them in fields for later use.

    @Service(Service.Level.APP, Service.Level.PROJECT)
    private class CoroutineScopeService(val coroutineScope: CoroutineScope)

    /**
     * 得到应用级别的协程作用域。
     */
    fun getCoroutineScope(): CoroutineScope = service<CoroutineScopeService>().coroutineScope

    /**
     * 得到指定项目的协程作用域。
     */
    fun getCoroutineScope(project: Project): CoroutineScope = project.service<CoroutineScopeService>().coroutineScope

    /**
     * 得到应用级别的指定游戏类型的规则分组（不能用于访问 PSI）。
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
     * @param project 指定的项目。如果是默认项目，则会得到应用级别的规则分组（不能用于访问 PSI）。
     * @param gameType 指定的游戏类型。如果是 `null` 或 [ParadoxGameType.Core]，则会得到共享的规则分组。
     */
    fun getConfigGroup(project: Project, gameType: ParadoxGameType? = null): CwtConfigGroup {
        val finalGameType = gameType ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance(project).getConfigGroup(finalGameType)
    }

    /**
     * 从 [event] 得到对应的规则分组。
     */
    fun getConfigGroup(event: AnActionEvent): CwtConfigGroup {
        val project = event.project
        val gameType = selectGameType(event.getData(CommonDataKeys.VIRTUAL_FILE))
        val finalGameType = gameType ?: ParadoxGameType.Core
        return CwtConfigGroupService.getInstance(project).getConfigGroup(finalGameType)
    }

    /**
     * 检查指定项目与上下文的规则分组是否已加载完毕。
     *
     * @param project 指定的项目。
     * @param context 用于获取游戏类型的上下文对象。
     */
    fun checkConfigGroupInitialized(project: Project, context: Any?): Boolean {
        return CwtConfigGroupService.getInstance(project).checkConfigGroupInitialized(context)
    }

    /** 检查是否正在进行单元测试，或者 IDE 是否正处于单元测试模式。 */
    fun isUnitTestMode(): Boolean {
        return ApplicationManager.getApplication().let { it == null || it.isUnitTestMode }
    }

    /** 检查 IDE 是否正处于内部模式。 */
    fun isInternal(): Boolean {
        return ApplicationManager.getApplication().let { it != null && it.isInternal }
    }

    // TODO [compatibility] `PluginManagerCore.getPlugin(PluginId)` is internal since IDEA-262 - Commented out since this method is currently not used
    // /** 是否是开发中版本。 */
    // fun isDevVersion(): Boolean {
    //     return PluginManagerCore.getPlugin(PlsConstants.pluginId)?.version?.endsWith("-dev") == true
    // }
}

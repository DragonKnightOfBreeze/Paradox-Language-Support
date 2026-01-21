package icu.windea.pls.lang.util

import com.intellij.ide.DataManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.project.Project
import org.jetbrains.concurrency.Promise

@Suppress("unused")
object PlsUiManager {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Configurable> showSettingsDialog(project: Project? = null, id: String) {
        // 如果在设置页面中使用，则会嵌套打开另一个设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog(project, id)
    }

    inline fun <reified T : Configurable> showSettingsDialog(project: Project? = null) {
        // 如果在设置页面中使用，则会嵌套打开另一个设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog<T>(project, T::class.java)
    }

    inline fun <R> withConfigurable(id: String, crossinline block: (settings: Settings, configurable: Configurable) -> R): Promise<R> {
        return DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find(id)?.let { configurable ->
                    block(settings, configurable)
                }
            }
        }
    }

    inline fun <reified T : Configurable, R> withConfigurable(crossinline block: (settingss: Settings, configurable: T) -> R): Promise<R> {
        return DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find(T::class.java)?.let { configurable ->
                    block(settings, configurable)
                }
            }
        }
    }
}

package icu.windea.pls.lang.util

import com.intellij.ide.DataManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.project.Project

@Suppress("unused")
object PlsUiManager {
    fun <T : Configurable> showSettingsDialog(project: Project? = null, id: String) {
        // 如果在设置页面中使用，则会嵌套打开另一个设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog(project, id)
    }

    fun <T : Configurable> showSettingsDialog(project: Project? = null, type: Class<T>) {
        // 如果在设置页面中使用，则会嵌套打开另一个设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog(project, type)
    }

    inline fun <reified T : Configurable> showSettingsDialog(project: Project? = null) {
        showSettingsDialog(project, T::class.java)
    }

    fun selectSettings(id: String, option: String? = null) {
        // 直接跳转到指定的设置页面
        DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find(id)?.let { configurable ->
                    if (option == null) {
                        settings.select(configurable)
                    } else {
                        settings.select(configurable, option)
                    }
                }
            }
        }
    }

    fun <T : Configurable> selectSettings(type: Class<T>, option: String? = null) {
        // 直接跳转到指定的设置页面
        DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find(type)?.let { configurable ->
                    if (option == null) {
                        settings.select(configurable)
                    } else {
                        settings.select(configurable, option)
                    }
                }
            }
        }
    }

    inline fun <reified T : Configurable> selectSettings(option: String? = null) {
        selectSettings(T::class.java, option)
    }
}

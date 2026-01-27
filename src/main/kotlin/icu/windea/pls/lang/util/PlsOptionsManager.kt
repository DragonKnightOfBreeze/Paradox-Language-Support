package icu.windea.pls.lang.util

import com.intellij.ide.DataManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.options.ex.Settings
import com.intellij.openapi.project.Project
import icu.windea.pls.core.memberFunction
import icu.windea.pls.core.runCatchingCancelable

@Suppress("unused")
object PlsOptionsManager {
    fun <T : Configurable> showSettingsDialog(project: Project? = null, id: String) {
        // 如果在设置页面中使用，则会嵌套打开另一个设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog(project, id)
    }

    fun <T : Configurable> showSettingsDialog(project: Project? = null, toSelect: Class<T>) {
        // 如果在设置页面中使用，则会嵌套打开另一个设置页面
        ShowSettingsUtil.getInstance().showSettingsDialog<T>(project, toSelect)
    }

    inline fun <reified T : Configurable> showSettingsDialog(project: Project? = null) {
        showSettingsDialog(project, T::class.java)
    }

    fun select(id: String, option: String? = null) {
        // 如果在设置页面中使用，则会直接转到另一个设置页面
        DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find(id)?.let { configurable ->
                    if (option != null) {
                        settings.select(configurable, option)
                    } else {
                        settings.select(configurable)
                    }
                }
            }
        }
    }

    fun <T : Configurable> select(type: Class<T>, option: String? = null) {
        // 如果在设置页面中使用，则会直接转到另一个设置页面
        DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find(type)?.let { configurable ->
                    if (option != null) {
                        settings.select(configurable, option)
                    } else {
                        settings.select(configurable)
                    }
                }
            }
        }
    }

    inline fun <reified T : Configurable> select(option: String? = null) {
        select(T::class.java, option)
    }

    fun selectPlugin(option: String, openMarketplaceTab: Boolean = false) {
        // 如果在设置页面中使用，则会直接转到另一个设置页面
        DataManager.getInstance().dataContextFromFocusAsync.then {
            Settings.KEY.getData(it)?.let { settings ->
                settings.find("preferences.pluginManager")?.let { configurable ->
                    if (openMarketplaceTab) tryOpenMarketplaceTab(configurable, option)
                    settings.select(configurable, option)
                }
            }
        }
    }

    private fun tryOpenMarketplaceTab(configurable: Configurable, option: String) {
        // 我不用这个内部 API 怎么实现这个功能.png
        // com.intellij.ide.plugins.PluginManagerConfigurable.openMarketplaceTab
        val function = memberFunction("openMarketplaceTab", "com.intellij.ide.plugins.PluginManagerConfigurable")
        runCatchingCancelable { function(configurable, option) }
    }
}

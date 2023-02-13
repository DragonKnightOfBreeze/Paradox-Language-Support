package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*

/**
 * 当更改模组配置后，更新库信息。
 */
class ParadoxUpdateLibraryOnModSettingsChangedListener: ParadoxModSettingsListener {
    override fun onChange(project: Project, modSettings: ParadoxModSettingsState) {
        //TODO 0.8.1 优化 - 限定在单个项目范围
        val allModSettings = getAllModSettings()
        allModSettings.roots = allModSettings.computeRoots()
    }
}
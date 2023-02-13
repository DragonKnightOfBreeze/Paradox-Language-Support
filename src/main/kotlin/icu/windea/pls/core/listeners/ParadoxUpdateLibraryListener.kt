package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*

/**
 * 当更改模组配置后，更新库信息。
 */
class ParadoxUpdateLibraryListener: ParadoxModSettingsListener {
    override fun onChange(project: Project, modSettings: ParadoxModSettingsState) {
        //TODO 优化
        val allModSettings = getAllModSettings(project)
        allModSettings.roots = allModSettings.computeRoots()
    }
}
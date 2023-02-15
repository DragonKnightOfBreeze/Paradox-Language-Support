package icu.windea.pls.core.listeners

import icu.windea.pls.*
import icu.windea.pls.core.settings.*

/**
 * 当更改模组配置后，更新库信息。
 */
class ParadoxUpdateLibraryOnModSettingsChangedListener: ParadoxModSettingsListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val allModSettings = getAllModSettings()
        allModSettings.roots = allModSettings.computeRoots()
    }
}
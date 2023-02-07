package icu.windea.pls.core.settings

import com.intellij.openapi.project.Project
import icu.windea.pls.*

class ParadoxUpdateLibraryOnModSettingsChangedListener: ParadoxModSettingsChangedListener {
    override fun onChanged(project: Project, modSettings: ParadoxModSettingsState) {
        //TODO 优化
        val allModSettings = getAllModSettings(project)
        allModSettings.roots = allModSettings.computeRoots()
    }
}
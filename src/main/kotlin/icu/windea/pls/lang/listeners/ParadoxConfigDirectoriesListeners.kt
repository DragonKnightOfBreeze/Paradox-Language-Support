package icu.windea.pls.lang.listeners

import com.intellij.openapi.components.*
import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.openapi.project.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*

/**
 * 当更改本地规则目录后，刷新库信息。
 *
 * @see CwtConfigGroupLibrary
 * @see CwtConfigGroupLibraryProvider
 */
class ParadoxUpdateLibraryOnConfigDirectoriesChangedListener : ParadoxConfigDirectoriesListener {
    override fun onChange() {
        doUpdate()
    }

    private fun doUpdate() {
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val library = project.configGroupLibrary
            library.refreshRoots()
        }
    }
}

/**
 * 当更改本地规则目录后，刷新规则分组的修改状态。
 *
 * @see CwtConfigGroup
 * @see ConfigGroupRefreshFloatingProvider
 */
class ParadoxUpdateConfigGroupOnConfigDirectoriesChangedListener : ParadoxConfigDirectoriesListener {
    override fun onChange() {
        doUpdate()
    }

    private fun doUpdate() {
        for (project in ProjectManager.getInstance().openProjects) {
            if (project.isDisposed) continue
            val configGroupService = project.service<CwtConfigGroupService>()
            configGroupService.getConfigGroups().values.forEach { configGroup ->
                configGroup.changed.set(true)
            }
            FloatingToolbarProvider.EP_NAME.findExtensionOrFail(ConfigGroupRefreshFloatingProvider::class.java).updateToolbarComponents(project)
        }
    }
}

package icu.windea.pls.lang.listeners

import com.intellij.openapi.project.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*

/**
 * 当更改本地规则目录后，刷新库信息。
 *
 * @see CwtConfigGroupLibrary
 * @see CwtConfigGroupLibraryProvider
 */
class ParadoxUpdateLibraryOnLocalConfigDirectoryChangedListener : ParadoxLocalConfigDirectoryListener {
    override fun onChange(oldDirectory: String, newDirectory: String) {
        doUpdateLibrary()
    }
    
    private fun doUpdateLibrary() {
        for(project in ProjectManager.getInstance().openProjects) {
            if(project.isDisposed) continue
            val library = project.configGroupLibrary
            library.refreshRoots()
        }
    }
}
package icu.windea.pls.lang.listeners

import com.intellij.openapi.project.*
import com.intellij.openapi.startup.*
import icu.windea.pls.lang.*

/**
 * 项目启动时刷新库信息。
 */
class ParadoxUpdateLibraryOnProjectOpenedListener: ProjectActivity {
    override suspend fun execute(project: Project) {
        val paradoxLibrary = project.paradoxLibrary
        paradoxLibrary.refreshRoots()
    }
}

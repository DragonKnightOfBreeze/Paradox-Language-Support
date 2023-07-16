package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import com.intellij.openapi.startup.*
import icu.windea.pls.core.*

/**
 * 项目启动时刷新库信息。
 */
class ParadoxUpdateLibraryOnProjectOpenedListener: ProjectActivity {
    override suspend fun execute(project: Project) {
        val paradoxLibrary = project.paradoxLibrary
        paradoxLibrary.refreshRoots()
    }
}

package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.configGroupLibrary
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton

// each library each project

class CwtConfigGroupLibraryProvider : AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return project.configGroupLibrary.takeIf { it.roots.isNotEmpty() }.singleton.setOrEmpty()
    }

    override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
        return project.configGroupLibrary.roots
    }
}

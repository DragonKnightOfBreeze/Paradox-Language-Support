package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.*
import icu.windea.pls.core.collections.*

//each library each project

class CwtConfigGroupLibraryProvider: AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return project.configGroupLibrary.takeIf { it.roots.isNotEmpty() }.toSingletonSetOrEmpty()
    }
    
    override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
        //filter out in-project roots
        val roots = project.configGroupLibrary.roots
        if(roots.isEmpty()) return emptySet()
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        return roots.filter { !projectFileIndex.isInContent(it) }
    }
}
package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.collections.*

//each library each project

class ParadoxLibraryProvider : AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return project.paradoxLibrary.takeIf { it.roots.isNotEmpty() }.toSingletonSetOrEmpty()
    }
    
    override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
        return project.paradoxLibrary.roots
    }
}
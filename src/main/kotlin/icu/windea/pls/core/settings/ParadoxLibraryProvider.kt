package icu.windea.pls.core.settings

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

class ParadoxLibraryProvider : AdditionalLibraryRootsProvider() {
    // each library each project
    // the paradox library will contain all roots from project mod settings, aka all game and mod dependency directories 
    
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return project.getOrPutUserData(PlsKeys.libraryKey) {
            ParadoxLibrary(project)
        }.toSingletonSet()
    }
    
    override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
        return getAllModSettings(project).roots
    }
}
package icu.windea.pls.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to

// each library each project

class ParadoxLibraryProvider : AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        val library = ParadoxLibraryService.getInstance(project).library
        if (library.roots.isEmpty()) return emptySet()
        return library.to.singletonSetOrEmpty()
    }

    override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
        val library = ParadoxLibraryService.getInstance(project).library
        return library.roots
    }
}

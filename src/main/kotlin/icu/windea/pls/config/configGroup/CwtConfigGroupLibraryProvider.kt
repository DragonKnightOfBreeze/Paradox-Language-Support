package icu.windea.pls.config.configGroup

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to

// each library each project

class CwtConfigGroupLibraryProvider : AdditionalLibraryRootsProvider() {
    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        val library = CwtConfigGroupLibraryService.getInstance(project).library
        if (library.roots.isEmpty()) return emptySet()
        return library.to.singletonSetOrEmpty()
    }

    override fun getRootsToWatch(project: Project): Collection<VirtualFile> {
        val library = CwtConfigGroupLibraryService.getInstance(project).library
        return library.roots
    }
}

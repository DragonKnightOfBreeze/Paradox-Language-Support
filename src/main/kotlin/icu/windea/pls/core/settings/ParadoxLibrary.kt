package icu.windea.pls.core.settings

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

class ParadoxLibrary(val project: Project) : SyntheticLibrary() {
    override fun getSourceRoots(): Collection<VirtualFile> {
        return getAllModSettings(project).roots
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxLibrary && project == other.project)
    }
    
    override fun hashCode(): Int {
        return project.hashCode()
    }
}
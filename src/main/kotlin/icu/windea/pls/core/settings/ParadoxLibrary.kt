package icu.windea.pls.core.settings

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.*
import javax.swing.*

class ParadoxLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    override fun getSourceRoots(): Collection<VirtualFile> {
        //TODO 0.8.1 优化 - 限定在单个项目范围
        return getAllModSettings().roots
    }
    
    override fun isShowInExternalLibrariesNode(): Boolean {
        return true
    }
    
    override fun getIcon(unused: Boolean): Icon {
        return PlsIcons.Library
    }
    
    override fun getPresentableText(): String {
        return PlsBundle.message("library.name")
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxLibrary && project == other.project)
    }
    
    override fun hashCode(): Int {
        return project.hashCode()
    }
}
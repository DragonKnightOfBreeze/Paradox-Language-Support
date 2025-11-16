package icu.windea.pls.config.configGroup

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import javax.swing.Icon

/**
 * 非项目本地的规则目录对应的外部库。
 *
 * @see CwtConfigGroupLibraryService
 * @see CwtConfigGroupLibraryProvider
 */
class CwtConfigGroupLibrary(val project: Project) : SyntheticLibrary(), ItemPresentation {
    @Volatile
    var roots: Set<VirtualFile> = emptySet()

    override fun getSourceRoots(): Collection<VirtualFile> {
        return roots
    }

    override fun isShowInExternalLibrariesNode(): Boolean {
        return true
    }

    override fun getIcon(unused: Boolean): Icon {
        return PlsIcons.General.Library
    }

    override fun getPresentableText(): String {
        return PlsBundle.message("configGroup.library.name")
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtConfigGroupLibrary && project == other.project
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }

    override fun toString(): String {
        return "CwtConfigGroupLibrary(project=$project)"
    }
}

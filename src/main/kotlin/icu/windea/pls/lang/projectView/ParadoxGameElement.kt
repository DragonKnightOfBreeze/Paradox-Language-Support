package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.RootsProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.model.ParadoxGameType
import java.util.*

class ParadoxGameElement(
    val project: Project,
    val gameType: ParadoxGameType,
    val preferredRootFile: VirtualFile?
) : RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val directories = mutableListOf<VirtualFile>()
        val selector = selector(project, preferredRootFile).file().withGameType(gameType)
        val files = ParadoxFilePathSearch.search("", null, selector).findAll()
        files.forEach { file ->
            if (file.isDirectory) {
                directories.add(file)
            }
        }
        return directories
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is ParadoxGameElement && project == other.project && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(project, gameType)
    }
}

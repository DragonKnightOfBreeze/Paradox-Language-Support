package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import java.util.*

class ParadoxDirectoryElement(
    val project: Project,
    val path: ParadoxPath,
    val gameType: ParadoxGameType,
) : RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val directories = mutableListOf<VirtualFile>()
        val selector = fileSelector(project).withGameType(gameType)
        val files = ParadoxFilePathSearch.search(path.path, null, selector).findAll()
        files.forEach { file ->
            if(file.isDirectory) {
                directories.add(file)
            }
        }
        return directories
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxDirectoryElement && project == other.project && path == other.path && gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(project, path, gameType)
    }
}
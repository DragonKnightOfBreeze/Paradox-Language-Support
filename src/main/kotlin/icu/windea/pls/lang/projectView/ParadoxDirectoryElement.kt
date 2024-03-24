package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.model.path.*
import java.util.*

class ParadoxDirectoryElement(
    val project: Project,
    val path: ParadoxPath,
    val gameType: ParadoxGameType,
    val preferredRootFile: VirtualFile?,
) : RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val roots = mutableSetOf<VirtualFile>()
        val selector = fileSelector(project, preferredRootFile).withGameType(gameType)
        val files = ParadoxFilePathSearch.search(path.path, null, selector).findAll()
        files.forEach { file ->
            if(file.isDirectory) roots += file
        }
        return roots
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxDirectoryElement && project == other.project && path == other.path && gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(project, path, gameType)
    }
}


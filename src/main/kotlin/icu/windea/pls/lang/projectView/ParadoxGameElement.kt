package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.model.*
import java.util.*

class ParadoxGameElement(
    val project: Project,
    val gameType: ParadoxGameType,
    val preferredRootFile: VirtualFile?
): RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val directories = mutableListOf<VirtualFile>()
        val selector = fileSelector(project, preferredRootFile).withGameType(gameType)
        val files = ParadoxFilePathSearch.search("", null, selector).findAll()
        files.forEach { file ->
            if(file.isDirectory) {
                directories.add(file)
            }
        }
        return directories
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is ParadoxGameElement && project == other.project && gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(project, gameType)
    }
}
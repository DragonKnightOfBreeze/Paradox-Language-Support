package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*
import java.util.*

class CwtConfigGameElement(
    val project: Project,
    val gameType: ParadoxGameType?
): RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val roots = mutableSetOf<VirtualFile>()
        val gameTypeId = gameType.id
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEachFast f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val file = rootDirectory.findChild(gameTypeId) ?: return@f
            if(file.isDirectory) roots += file
        }
        return roots
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other is CwtConfigGameElement && project == other.project && gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(project, gameType)
    }
}

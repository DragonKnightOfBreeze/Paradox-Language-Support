package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*
import java.util.*

class CwtConfigGameElement(
    val project: Project,
    val gameType: ParadoxGameType?
) : RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val roots = mutableSetOf<VirtualFile>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val nodeFile = rootDirectory.findChild(directoryName) ?: return@f
            if (nodeFile.isDirectory) roots += nodeFile
        }
        return roots
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is CwtConfigGameElement && project == other.project && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(project, gameType)
    }
}

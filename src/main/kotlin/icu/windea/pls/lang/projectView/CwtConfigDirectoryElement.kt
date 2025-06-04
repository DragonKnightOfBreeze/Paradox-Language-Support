package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*
import java.util.*

class CwtConfigDirectoryElement(
    val project: Project,
    val path: String,
    val gameType: ParadoxGameType?,
) : RootsProvider {
    override fun getRoots(): Collection<VirtualFile> {
        val roots = mutableSetOf<VirtualFile>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val relativePath = "$directoryName/$path"
            val file = VfsUtil.findRelativeFile(rootDirectory, relativePath) ?: return@f
            if (file.isDirectory) roots += file
        }
        return roots
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is CwtConfigDirectoryElement && project == other.project && path == other.path && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(project, path, gameType)
    }
}


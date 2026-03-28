package icu.windea.pls.ep.analysis

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.lang.settings.PlsSettings

class ParadoxSettingsBasedIgnoredFileProvider : ParadoxIgnoredFileProvider {
    override fun isIgnoredFile(file: VirtualFile): Boolean {
        return isIgnoredFile(file.name)
    }

    override fun isIgnoredFile(filePath: FilePath): Boolean {
        return isIgnoredFile(filePath.name)
    }

    private fun isIgnoredFile(fileName: String): Boolean {
        return fileName in PlsSettings.getInstance().state.ignoredFileNameSet
    }
}

class ParadoxForcedIgnoredFileProvider : ParadoxIgnoredFileProvider {
    // e.g.
    // - readme.txt
    // - credits.txt
    // - 99_README_CONCEPTS.txt

    override fun isIgnoredFile(file: VirtualFile): Boolean {
        return isIgnoredFile(file.name)
    }

    override fun isIgnoredFile(filePath: FilePath): Boolean {
        return isIgnoredFile(filePath.name)
    }

    private fun isIgnoredFile(fileName: String): Boolean {
        val name = fileName.lowercase()
        when {
            name == "readme.txt" -> return true
            name == "changelog.txt" -> return true
            name == "license.txt" -> return true
            name == "credits.txt" -> return true
            name.matchesRegex("""(?:\d+_)?readme(?:_\w+)?.txt""") -> return true
            else -> return false
        }
    }
}

package icu.windea.pls.ep.analysis

import icu.windea.pls.core.matchesRegex
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.model.paths.matchesExtension

class ParadoxSettingsBasedIgnoredFileProvider : ParadoxIgnoredFileProvider {
    // based on:
    // - ignoredFileNameSet

    override fun isIgnoredFile(path: ParadoxPath, entry: String): Boolean {
        return path.fileName in PlsSettings.getInstance().state.ignoredFileNameSet
    }

}

class ParadoxTopLevelIgnoredFileProvider : ParadoxIgnoredFileProvider {
    // rules:
    // - ignore top level .txt files

    override fun isIgnoredFile(path: ParadoxPath, entry: String): Boolean {
        return path.parent.isEmpty() && path.matchesExtension("txt")
    }
}

class ParadoxForcedIgnoredFileProvider : ParadoxIgnoredFileProvider {
    // e.g.
    // - readme.txt
    // - credits.txt
    // - 99_README_CONCEPTS.txt

    override fun isIgnoredFile(path: ParadoxPath, entry: String): Boolean {
        val fileName = path.fileName.lowercase()
        return when {
            fileName == "readme.txt" -> true
            fileName == "changelog.txt" -> true
            fileName == "license.txt" -> true
            fileName == "credits.txt" -> true
            fileName.matchesRegex("""(?:\d+_)?readme(?:_\w+)?.txt""") -> true
            else -> false
        }
    }
}

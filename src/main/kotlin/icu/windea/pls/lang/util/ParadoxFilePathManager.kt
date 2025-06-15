package icu.windea.pls.lang.util

import com.intellij.injected.editor.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.PlsConstants
import java.lang.invoke.*

object ParadoxFilePathManager {
    object Keys : KeyRegistry() {
        val fileExtensions by createKey<Set<String>>(this)
    }

    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    const val scriptedVariablesPath = "common/scripted_variables"

    fun getScriptedVariablesDirectory(contextFile: VirtualFile): VirtualFile? {
        val rootInfo = contextFile.fileInfo?.rootInfo ?: return null
        val entryFile = rootInfo.entryFile
        val path = scriptedVariablesPath
        VfsUtil.createDirectoryIfMissing(entryFile, path)
        return entryFile.findFileByRelativePath(path)
    }

    fun canBeScriptOrLocalisationFile(filePath: FilePath): Boolean {
        //val fileName = filePath.name.lowercase()
        val fileExtension = filePath.name.substringAfterLast('.').orNull()?.lowercase() ?: return false
        return when {
            fileExtension == "mod" -> true
            fileExtension in PlsConstants.scriptFileExtensions -> true
            fileExtension in PlsConstants.localisationFileExtensions -> true
            else -> false
        }
    }

    fun canBeScriptOrLocalisationFile(file: VirtualFile): Boolean {
        //require pre-check from user data
        //require further check for VirtualFileWindow (injected PSI)

        if (file is VirtualFileWithoutContent) return false
        if (file is VirtualFileWindow) return true
        //val fileName = file.name.lowercase()
        val fileExtension = file.extension?.lowercase() ?: return false
        return when {
            fileExtension == "mod" -> true
            fileExtension in PlsConstants.scriptFileExtensions -> true
            fileExtension in PlsConstants.localisationFileExtensions -> true
            else -> false
        }
    }

    fun canBeScriptFilePath(path: ParadoxPath): Boolean {
        if (inLocalisationPath(path)) return false
        val fileExtension = path.fileExtension?.lowercase() ?: return false
        if (fileExtension !in PlsConstants.scriptFileExtensions) return false
        return true
    }

    fun canBeLocalisationFilePath(path: ParadoxPath): Boolean {
        if (!inLocalisationPath(path)) return false
        val fileExtension = path.fileExtension?.lowercase() ?: return false
        if (fileExtension !in PlsConstants.localisationFileExtensions) return false
        return true
    }

    fun inLocalisationPath(path: ParadoxPath, synced: Boolean? = null): Boolean {
        val root = path.root
        if (synced != true) {
            if (root == "localisation" || root == "localization") return true
        }
        if (synced != false) {
            if (root == "localisation_synced" || root == "localization_synced") return true
        }
        return false
    }

    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.fileExtensions) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }
}

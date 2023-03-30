package icu.windea.pls.core.actions

import com.intellij.ide.actions.CreateDirectoryCompletionContributor
import com.intellij.ide.actions.CreateDirectoryCompletionContributor.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import com.intellij.util.Processor
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*

/**
 * 在游戏目录或模组根目录下创建目录时，可以提示目录名称
 */
class ParadoxCreateDirectoryCompletionContributor : CreateDirectoryCompletionContributor {
    val defaultVariants = setOf(
        "common",
        "events",
        "gfx",
        "interface",
        "localisation",
    )
    
    override fun getDescription(): String {
        return PlsBundle.message("create.directory.completion.description")
    }
    
    //基于已有的包含脚本文件、本地化文件或者DDS/PNG/TGA文件的目录
    
    override fun getVariants(directory: PsiDirectory): Collection<Variant> {
        val fileInfo = directory.fileInfo ?: return emptySet()
        val path = fileInfo.entryPath.path //use entryPath here
        val pathPrefix = if(path.isEmpty()) "" else path + "/"
        val result = sortedSetOf<String>()
        if(path.isEmpty()) {
            result.addAll(defaultVariants)
        }
        val project = directory.project
        val name = ParadoxFilePathIndex.NAME
        FileBasedIndex.getInstance().processAllKeys(name, Processor {
            ProgressManager.checkCanceled()
            var p = it
            p = p.removePrefixOrNull(pathPrefix)
            if(p != null && p.isNotEmpty()) {
                if(isParadoxFile(p)) {
                    p = p.substringBeforeLast('/', "")
                    if(p.isNotEmpty()) {
                        result.add(p)
                    }
                }
            }
            true
        }, project)
        return result.map { it.toVariant() }
    }
    
    private fun isParadoxFile(path: String): Boolean {
        val extension = path.substringAfterLast('.')
        if(extension.isEmpty()) return false
        return extension in PlsConstants.scriptFileExtensions
            || extension in PlsConstants.localisationFileExtensions
            || extension in PlsConstants.ddsFileExtensions
            || extension == "png"
            || extension == "tga"
    }
    
    private fun String.toVariant() = Variant(this, null)
}
package icu.windea.pls.core.actions

import com.intellij.ide.actions.CreateDirectoryCompletionContributor
import com.intellij.ide.actions.CreateDirectoryCompletionContributor.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 在游戏目录或模组根目录下创建目录时，可以提示目录名称
 */
class ParadoxCreateDirectoryCompletionContributor : CreateDirectoryCompletionContributor {
    val defaultVariants = setOf(
        "common".toVariant(),
        "events".toVariant(),
        "gfx".toVariant(),
        "interface".toVariant(),
        "localisation".toVariant()
    )
    
    override fun getDescription(): String {
        return PlsBundle.message("create.directory.completion.description")
    }
    
    //TODO 基于已有的包含脚本文件、本地化文件或者DDS文件的目录
    
    override fun getVariants(directory: PsiDirectory): Collection<Variant> {
        val fileInfo = directory.fileInfo ?: return emptySet()
        val filePath = fileInfo.entryPath.path //use entryPath here
        val result = sortedSetOf<Variant>(compareBy { it.path })
        if(filePath.isEmpty()) {
            result.addAll(defaultVariants)
        }
        return result
    }
    
    private fun String.toVariant() = Variant(this, null)
}
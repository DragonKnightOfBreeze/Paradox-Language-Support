package icu.windea.pls.lang

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*

object ParadoxFileHandler {
    const val scriptedVariablesPath = "common/scripted_variables"
    
    fun scriptedVariablesFileName(prefix: String) = "${prefix}scripted_variables.txt"
    
    fun getScriptedVariablesDirectory(context: VirtualFile): VirtualFile? {
        val root = getRootFile(context) ?: return null
        VfsUtil.createDirectoryIfMissing(root, scriptedVariablesPath)
        return root.findFileByRelativePath(scriptedVariablesPath)
    }
    
    fun getRootFile(context: VirtualFile): VirtualFile? {
        return context.fileInfo?.rootInfo?.gameRootFile
    }
    
    fun getGeneratedFileName(directory: VirtualFile): VirtualFile? {
        if(!directory.isDirectory) return null
        val directoryPath = directory.fileInfo?.pathToEntry ?: return null
        if(scriptedVariablesPath.matchesPath(directoryPath.path)) {
            val fileName = scriptedVariablesFileName(getSettings().generation.fileNamePrefix.orEmpty())
            return directory.findOrCreateChildData(ParadoxFileHandler, fileName)
        }
        return null
    }
    
    /**
     * 判断目标文件能否引用另一个文件中的内容。
     *
     * 对于某些蠢驴游戏来说，游戏目录下可以存在多个入口目录（entries）。
     * 认为模组目录以及主要入口目录（根目录或者game目录）不能引用次要入口目录（非根目录或者game目录）下的文件中的内容。
     * 认为不同的次要入口目录下的文件不能互相引用其内容。
     */
    fun canReference(targetFile: VirtualFile?, otherFile: VirtualFile?): Boolean {
		val target = targetFile?.fileInfo ?: return true
		val other = otherFile?.fileInfo ?: return true
        if(target.isMainEntry()) {
            if(!other.isMainEntry()) return false
        } else {
            if(other.isMainEntry()) return true
            if(target.entry != other.entry) return false
        }
        return true
    }
}

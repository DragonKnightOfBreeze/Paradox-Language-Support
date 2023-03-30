package icu.windea.pls.core.index

import com.intellij.openapi.util.registry.*
import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import java.util.*

class ParadoxDirectoryFilePathIndex : ScalarIndexExtension<ParadoxFilePathInfo>() {
    companion object {
        @JvmField val NAME = ID.create<ParadoxFilePathInfo, Void>("paradox.directory.file.path.index")
    }
    
    override fun getName(): ID<ParadoxFilePathInfo, Void> {
        return NAME
    }
    
    override fun getIndexer(): DataIndexer<ParadoxFilePathInfo, Void, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.file
            val fileInfo = file.fileInfo ?: return@DataIndexer emptyMap()
            val path = when {
                !file.isValid -> return@DataIndexer emptyMap()
                file.isDirectory -> fileInfo.path.path
                else -> fileInfo.path.parent
            }
            val gameType = fileInfo.rootInfo.gameType
            val info = ParadoxFilePathInfo(path, gameType)
            Collections.singletonMap<ParadoxFilePathInfo, Void>(info, null)
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<ParadoxFilePathInfo> {
        return ParadoxFilePathKeyDescriptor
    }
    
    override fun getVersion(): Int {
        return 4 + (if(Registry.`is`("indexing.paradox.file.path.over.vfs")) 0xff else 0)
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { filterFile(it) }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return false
    }
    
    private fun filterFile(file: VirtualFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val path = fileInfo.path.path
        val extension = path.substringAfterLast('.')
        if(extension.isEmpty()) return false
        return extension in PlsConstants.scriptFileExtensions
            || extension in PlsConstants.localisationFileExtensions
            || extension in PlsConstants.ddsFileExtensions
            || extension == "png"
            || extension == "tga"
    }
}
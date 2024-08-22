package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.io.*
import java.util.*

/**
 * 用于索引文件的路径信息。
 */
class ParadoxFilePathIndex : FileBasedIndexExtension<String, ParadoxFilePathInfo>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findIndex<ParadoxFilePathIndex>() }
        val NAME = ID.create<String, ParadoxFilePathInfo>("paradox.file.path.index")
        
        private const val VERSION = 53 //1.3.19
        
        private val EXCLUDED_DIRECTORIES = listOf(
            "_CommonRedist",
            "crash_reporter",
            "curated_save_games",
            "pdx_browser",
            "pdx_launcher",
            "pdx_online_assets",
            "previewer_assets",
            "jomini",
        )
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, ParadoxFilePathInfo, FileContent> {
        return DataIndexer { inputData ->
            //这里索引的路径，使用相对于入口目录的路径
            val fileInfo = inputData.file.fileInfo ?: return@DataIndexer emptyMap()
            val path = fileInfo.pathToEntry.path
            val directoryPath = fileInfo.pathToEntry.parent
            val gameType = fileInfo.rootInfo.gameType
            val included = isIncluded(inputData.file)
            val info = ParadoxFilePathInfo(directoryPath, gameType, included)
            Collections.singletonMap(path, info)
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    
    override fun getValueExternalizer(): DataExternalizer<ParadoxFilePathInfo> {
        return object : DataExternalizer<ParadoxFilePathInfo> {
            override fun save(storage: DataOutput, value: ParadoxFilePathInfo) {
                storage.writeUTFFast(value.directory)
                storage.writeByte(value.gameType.optimizeValue())
                storage.writeBoolean(value.included)
            }
            
            override fun read(storage: DataInput): ParadoxFilePathInfo {
                val path = storage.readUTFFast()
                val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
                val included = storage.readBoolean()
                return ParadoxFilePathInfo(path, gameType, included)
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null }
    }
    
    override fun indexDirectories(): Boolean {
        return true
    }
    
    override fun dependsOnFileContent(): Boolean {
        return false
    }
    
    private fun isIncluded(file: VirtualFile): Boolean {
        if(file.fileInfo == null) return false
        val parent = file.parent
        if(parent != null && parent.fileInfo != null && !isIncluded(parent)) return false
        val fileName = file.name
        if(fileName.startsWith('.')) return false //排除隐藏目录或文件
        if(file.isDirectory) {
            if(fileName in EXCLUDED_DIRECTORIES) return false //排除一些特定的目录
            return true
        }
        val extension = fileName.substringAfterLast('.')
        if(extension.isEmpty()) return false
        return extension in PlsConstants.scriptFileExtensions
            || extension in PlsConstants.localisationFileExtensions
            || extension in PlsConstants.imageFileExtensions
    }
}

